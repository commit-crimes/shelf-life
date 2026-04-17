#!/usr/bin/env python3
"""Generates ShelfLife.xcodeproj/project.pbxproj"""
import os, uuid, textwrap

def uid():
    return uuid.uuid4().hex.upper()[:24]

# All Swift source files relative to ShelfLife/
SOURCE_FILES = [
    "ShelfLifeApp.swift",
    "Models/FoodFacts.swift",
    "Models/FoodItem.swift",
    "Models/Recipe.swift",
    "Models/Household.swift",
    "Models/SLUser.swift",
    "Models/Invitation.swift",
    "Services/AuthService.swift",
    "Services/UserService.swift",
    "Services/HouseholdService.swift",
    "Services/FoodItemService.swift",
    "Services/RecipeService.swift",
    "Services/InvitationService.swift",
    "Services/OpenFoodFactsService.swift",
    "Services/OpenAIService.swift",
    "ViewModels/AuthViewModel.swift",
    "ViewModels/OverviewViewModel.swift",
    "ViewModels/FoodItemViewModel.swift",
    "ViewModels/BarcodeScannerViewModel.swift",
    "ViewModels/RecipesViewModel.swift",
    "ViewModels/RecipeGenerationViewModel.swift",
    "ViewModels/RecipeExecutionViewModel.swift",
    "ViewModels/ProfileViewModel.swift",
    "ViewModels/InvitationViewModel.swift",
    "ViewModels/LeaderboardViewModel.swift",
    "ViewModels/HouseholdViewModel.swift",
    "Views/Authentication/SignInView.swift",
    "Views/Navigation/MainTabView.swift",
    "Views/Navigation/HouseholdDrawerView.swift",
    "Views/Overview/OverviewView.swift",
    "Views/Overview/AddFoodItemView.swift",
    "Views/Overview/FoodItemDetailView.swift",
    "Views/Overview/HouseholdCreationView.swift",
    "Views/Scanner/BarcodeScannerView.swift",
    "Views/Recipes/RecipesView.swift",
    "Views/Recipes/RecipeDetailView.swift",
    "Views/Recipes/AddRecipeView.swift",
    "Views/Recipes/GenerateRecipeView.swift",
    "Views/Recipes/RecipeExecutionView.swift",
    "Views/Profile/ProfileView.swift",
    "Views/Invitations/InvitationsView.swift",
    "Views/Leaderboard/LeaderboardView.swift",
]

RESOURCE_FILES = [
    "Resources/Info.plist",
    "Resources/Assets.xcassets",
]

# UUIDs
PROJECT_ID       = uid()
TARGET_ID        = uid()
PRODUCT_REF      = uid()
SRC_PHASE_ID     = uid()
RES_PHASE_ID     = uid()
FWK_PHASE_ID     = uid()
DEBUG_CFG_ID     = uid()
RELEASE_CFG_ID   = uid()
PROJ_CFGLIST_ID  = uid()
TARGET_CFGLIST_ID= uid()
DEBUG_TARGET_CFG = uid()
RELEASE_TARGET_CFG = uid()

# Main group and sub-groups
ROOT_GROUP_ID    = uid()
PRODUCTS_GROUP   = uid()
MODELS_GROUP     = uid()
SERVICES_GROUP   = uid()
VIEWMODELS_GROUP = uid()
VIEWS_GROUP      = uid()
AUTH_VIEW_GROUP  = uid()
NAV_VIEW_GROUP   = uid()
OV_VIEW_GROUP    = uid()
SCAN_VIEW_GROUP  = uid()
REC_VIEW_GROUP   = uid()
PROF_VIEW_GROUP  = uid()
INV_VIEW_GROUP   = uid()
LB_VIEW_GROUP    = uid()
RESOURCES_GROUP  = uid()

# SPM packages
FIREBASE_PKG_REF  = uid()
GIDSIGNIN_PKG_REF = uid()

FIREBASE_AUTH_PROD    = uid()
FIREBASE_FIRESTORE_PROD = uid()
FIREBASE_STORAGE_PROD  = uid()
FIREBASE_CORE_PROD     = uid()
GIDSIGNIN_PROD         = uid()
GIDSIGNIN_SWIFT_PROD   = uid()

# Build file UUIDs for SPM products
FIREBASE_AUTH_BF    = uid()
FIREBASE_FIRESTORE_BF = uid()
FIREBASE_STORAGE_BF  = uid()
FIREBASE_CORE_BF     = uid()
GIDSIGNIN_BF         = uid()
GIDSIGNIN_SWIFT_BF   = uid()

# Per-file UUIDs
file_refs  = {f: uid() for f in SOURCE_FILES + RESOURCE_FILES}
build_files = {f: uid() for f in SOURCE_FILES}
res_build_files = {f: uid() for f in RESOURCE_FILES}

def pbx_file_ref(path, fref):
    name = os.path.basename(path)
    if path.endswith('.swift'):
        ftype = 'sourcecode.swift'
    elif path.endswith('.plist'):
        ftype = 'text.plist.xml'
    elif path.endswith('.xcassets'):
        ftype = 'folder.assetcatalog'
    else:
        ftype = 'file'
    return f'\t\t{fref} = {{isa = PBXFileReference; lastKnownFileType = {ftype}; path = "{name}"; sourceTree = "<group>"; }};'

def pbx_build_file(fref, bfid, settings=""):
    s = f'\t\t{bfid} = {{isa = PBXBuildFile; fileRef = {fref}; '
    if settings:
        s += f'settings = {{{settings}}}; '
    s += '};'
    return s

def group_children(paths, prefix=""):
    ids = []
    for p in paths:
        if prefix:
            if p.startswith(prefix) and '/' not in p[len(prefix):]:
                ids.append(file_refs[p])
        else:
            ids.append(file_refs[p])
    return ids

# Build the pbxproj content
lines = []
lines.append('// !$*UTF8*$!')
lines.append('{')
lines.append('\tarchiveVersion = 1;')
lines.append('\tclasses = {')
lines.append('\t};')
lines.append('\tobjectVersion = 56;')
lines.append('\tobjects = {')
lines.append('')

# --- PBXBuildFile ---
lines.append('/* Begin PBXBuildFile section */')
for f in SOURCE_FILES:
    lines.append(pbx_build_file(file_refs[f], build_files[f]))
for f in RESOURCE_FILES:
    lines.append(pbx_build_file(file_refs[f], res_build_files[f]))
# SPM build files
for bfid, prod in [
    (FIREBASE_CORE_BF, FIREBASE_CORE_PROD),
    (FIREBASE_AUTH_BF, FIREBASE_AUTH_PROD),
    (FIREBASE_FIRESTORE_BF, FIREBASE_FIRESTORE_PROD),
    (FIREBASE_STORAGE_BF, FIREBASE_STORAGE_PROD),
    (GIDSIGNIN_BF, GIDSIGNIN_PROD),
    (GIDSIGNIN_SWIFT_BF, GIDSIGNIN_SWIFT_PROD),
]:
    lines.append(f'\t\t{bfid} = {{isa = PBXBuildFile; productRef = {prod}; }};')
lines.append('/* End PBXBuildFile section */')
lines.append('')

# --- PBXFileReference ---
lines.append('/* Begin PBXFileReference section */')
# Product app
lines.append(f'\t\t{PRODUCT_REF} = {{isa = PBXFileReference; explicitFileType = wrapper.application; includeInIndex = 0; path = ShelfLife.app; sourceTree = BUILT_PRODUCTS_DIR; }};')
for f in SOURCE_FILES + RESOURCE_FILES:
    lines.append(pbx_file_ref(f, file_refs[f]))
lines.append('/* End PBXFileReference section */')
lines.append('')

# --- PBXFrameworksBuildPhase ---
lines.append('/* Begin PBXFrameworksBuildPhase section */')
lines.append(f'\t\t{FWK_PHASE_ID} = {{')
lines.append(f'\t\t\tisa = PBXFrameworksBuildPhase;')
lines.append(f'\t\t\tbuildActionMask = 2147483647;')
lines.append(f'\t\t\tfiles = (')
for bfid in [FIREBASE_CORE_BF, FIREBASE_AUTH_BF, FIREBASE_FIRESTORE_BF, FIREBASE_STORAGE_BF, GIDSIGNIN_BF, GIDSIGNIN_SWIFT_BF]:
    lines.append(f'\t\t\t\t{bfid},')
lines.append(f'\t\t\t);')
lines.append(f'\t\t\trunOnlyForDeploymentPostprocessing = 0;')
lines.append(f'\t\t}};')
lines.append('/* End PBXFrameworksBuildPhase section */')
lines.append('')

# --- PBXGroup ---
lines.append('/* Begin PBXGroup section */')

def write_group(gid, name, children_ids, path=None):
    lines.append(f'\t\t{gid} = {{')
    lines.append(f'\t\t\tisa = PBXGroup;')
    lines.append(f'\t\t\tchildren = (')
    for c in children_ids:
        lines.append(f'\t\t\t\t{c},')
    lines.append(f'\t\t\t);')
    if path:
        lines.append(f'\t\t\tpath = {path};')
    else:
        lines.append(f'\t\t\tname = "{name}";')
    lines.append(f'\t\t\tsourceTree = "<group>";')
    lines.append(f'\t\t}};')

# Sub-groups
models_files = [f for f in SOURCE_FILES if f.startswith('Models/')]
services_files = [f for f in SOURCE_FILES if f.startswith('Services/')]
viewmodel_files = [f for f in SOURCE_FILES if f.startswith('ViewModels/')]
auth_view_files = [f for f in SOURCE_FILES if f.startswith('Views/Authentication/')]
nav_view_files  = [f for f in SOURCE_FILES if f.startswith('Views/Navigation/')]
ov_view_files   = [f for f in SOURCE_FILES if f.startswith('Views/Overview/')]
scan_view_files = [f for f in SOURCE_FILES if f.startswith('Views/Scanner/')]
rec_view_files  = [f for f in SOURCE_FILES if f.startswith('Views/Recipes/')]
prof_view_files = [f for f in SOURCE_FILES if f.startswith('Views/Profile/')]
inv_view_files  = [f for f in SOURCE_FILES if f.startswith('Views/Invitations/')]
lb_view_files   = [f for f in SOURCE_FILES if f.startswith('Views/Leaderboard/')]

write_group(MODELS_GROUP,     'Models',          [file_refs[f] for f in models_files],      'Models')
write_group(SERVICES_GROUP,   'Services',        [file_refs[f] for f in services_files],    'Services')
write_group(VIEWMODELS_GROUP, 'ViewModels',      [file_refs[f] for f in viewmodel_files],   'ViewModels')
write_group(AUTH_VIEW_GROUP,  'Authentication',  [file_refs[f] for f in auth_view_files],   'Authentication')
write_group(NAV_VIEW_GROUP,   'Navigation',      [file_refs[f] for f in nav_view_files],    'Navigation')
write_group(OV_VIEW_GROUP,    'Overview',        [file_refs[f] for f in ov_view_files],     'Overview')
write_group(SCAN_VIEW_GROUP,  'Scanner',         [file_refs[f] for f in scan_view_files],   'Scanner')
write_group(REC_VIEW_GROUP,   'Recipes',         [file_refs[f] for f in rec_view_files],    'Recipes')
write_group(PROF_VIEW_GROUP,  'Profile',         [file_refs[f] for f in prof_view_files],   'Profile')
write_group(INV_VIEW_GROUP,   'Invitations',     [file_refs[f] for f in inv_view_files],    'Invitations')
write_group(LB_VIEW_GROUP,    'Leaderboard',     [file_refs[f] for f in lb_view_files],     'Leaderboard')

views_children = [AUTH_VIEW_GROUP, NAV_VIEW_GROUP, OV_VIEW_GROUP, SCAN_VIEW_GROUP,
                  REC_VIEW_GROUP, PROF_VIEW_GROUP, INV_VIEW_GROUP, LB_VIEW_GROUP]
write_group(VIEWS_GROUP, 'Views', views_children, 'Views')

resources_files_ids = [file_refs[f] for f in RESOURCE_FILES]
write_group(RESOURCES_GROUP, 'Resources', resources_files_ids, 'Resources')

write_group(PRODUCTS_GROUP, 'Products', [PRODUCT_REF])

# App-level sources (just ShelfLifeApp.swift)
app_root_files = [file_refs['ShelfLifeApp.swift']]

write_group(ROOT_GROUP_ID, 'ShelfLife',
    app_root_files + [MODELS_GROUP, SERVICES_GROUP, VIEWMODELS_GROUP, VIEWS_GROUP, RESOURCES_GROUP, PRODUCTS_GROUP],
    'ShelfLife')

lines.append('/* End PBXGroup section */')
lines.append('')

# --- PBXNativeTarget ---
lines.append('/* Begin PBXNativeTarget section */')
lines.append(f'\t\t{TARGET_ID} = {{')
lines.append(f'\t\t\tisa = PBXNativeTarget;')
lines.append(f'\t\t\tbuildConfigurationList = {TARGET_CFGLIST_ID};')
lines.append(f'\t\t\tbuildPhases = (')
lines.append(f'\t\t\t\t{SRC_PHASE_ID},')
lines.append(f'\t\t\t\t{RES_PHASE_ID},')
lines.append(f'\t\t\t\t{FWK_PHASE_ID},')
lines.append(f'\t\t\t);')
lines.append(f'\t\t\tbuildRules = (')
lines.append(f'\t\t\t);')
lines.append(f'\t\t\tdependencies = (')
lines.append(f'\t\t\t);')
lines.append(f'\t\t\tname = ShelfLife;')
lines.append(f'\t\t\tpackageProductDependencies = (')
for prod in [FIREBASE_CORE_PROD, FIREBASE_AUTH_PROD, FIREBASE_FIRESTORE_PROD, FIREBASE_STORAGE_PROD, GIDSIGNIN_PROD, GIDSIGNIN_SWIFT_PROD]:
    lines.append(f'\t\t\t\t{prod},')
lines.append(f'\t\t\t);')
lines.append(f'\t\t\tproductName = ShelfLife;')
lines.append(f'\t\t\tproductReference = {PRODUCT_REF};')
lines.append(f'\t\t\tproductType = "com.apple.product-type.application";')
lines.append(f'\t\t}};')
lines.append('/* End PBXNativeTarget section */')
lines.append('')

# --- PBXProject ---
lines.append('/* Begin PBXProject section */')
lines.append(f'\t\t{PROJECT_ID} = {{')
lines.append(f'\t\t\tisa = PBXProject;')
lines.append(f'\t\t\tattributes = {{')
lines.append(f'\t\t\t\tBuildIndependentTargetsInParallel = 1;')
lines.append(f'\t\t\t\tLastSwiftUpdateCheck = 1500;')
lines.append(f'\t\t\t\tLastUpgradeCheck = 1500;')
lines.append(f'\t\t\t\tTargetAttributes = {{')
lines.append(f'\t\t\t\t\t{TARGET_ID} = {{')
lines.append(f'\t\t\t\t\t\tCreatedOnToolsVersion = 15.0;')
lines.append(f'\t\t\t\t\t}};')
lines.append(f'\t\t\t\t}};')
lines.append(f'\t\t\t}};')
lines.append(f'\t\t\tbuildConfigurationList = {PROJ_CFGLIST_ID};')
lines.append(f'\t\t\tcompatibilityVersion = "Xcode 14.0";')
lines.append(f'\t\t\tdevelopmentRegion = en;')
lines.append(f'\t\t\thasScannedForEncodings = 0;')
lines.append(f'\t\t\tknownRegions = (')
lines.append(f'\t\t\t\ten,')
lines.append(f'\t\t\t\tBase,')
lines.append(f'\t\t\t);')
lines.append(f'\t\t\tmainGroup = {ROOT_GROUP_ID};')
lines.append(f'\t\t\tpackageReferences = (')
lines.append(f'\t\t\t\t{FIREBASE_PKG_REF},')
lines.append(f'\t\t\t\t{GIDSIGNIN_PKG_REF},')
lines.append(f'\t\t\t);')
lines.append(f'\t\t\tproductRefGroup = {PRODUCTS_GROUP};')
lines.append(f'\t\t\tprojectDirPath = "";')
lines.append(f'\t\t\tprojectRoot = "";')
lines.append(f'\t\t\ttargets = (')
lines.append(f'\t\t\t\t{TARGET_ID},')
lines.append(f'\t\t\t);')
lines.append(f'\t\t}};')
lines.append('/* End PBXProject section */')
lines.append('')

# --- PBXResourcesBuildPhase ---
lines.append('/* Begin PBXResourcesBuildPhase section */')
lines.append(f'\t\t{RES_PHASE_ID} = {{')
lines.append(f'\t\t\tisa = PBXResourcesBuildPhase;')
lines.append(f'\t\t\tbuildActionMask = 2147483647;')
lines.append(f'\t\t\tfiles = (')
for f in RESOURCE_FILES:
    lines.append(f'\t\t\t\t{res_build_files[f]},')
lines.append(f'\t\t\t);')
lines.append(f'\t\t\trunOnlyForDeploymentPostprocessing = 0;')
lines.append(f'\t\t}};')
lines.append('/* End PBXResourcesBuildPhase section */')
lines.append('')

# --- PBXSourcesBuildPhase ---
lines.append('/* Begin PBXSourcesBuildPhase section */')
lines.append(f'\t\t{SRC_PHASE_ID} = {{')
lines.append(f'\t\t\tisa = PBXSourcesBuildPhase;')
lines.append(f'\t\t\tbuildActionMask = 2147483647;')
lines.append(f'\t\t\tfiles = (')
for f in SOURCE_FILES:
    lines.append(f'\t\t\t\t{build_files[f]},')
lines.append(f'\t\t\t);')
lines.append(f'\t\t\trunOnlyForDeploymentPostprocessing = 0;')
lines.append(f'\t\t}};')
lines.append('/* End PBXSourcesBuildPhase section */')
lines.append('')

# --- XCRemoteSwiftPackageReference ---
lines.append('/* Begin XCRemoteSwiftPackageReference section */')
lines.append(f'\t\t{FIREBASE_PKG_REF} = {{')
lines.append(f'\t\t\tisa = XCRemoteSwiftPackageReference;')
lines.append(f'\t\t\trequirement = {{')
lines.append(f'\t\t\t\tkind = upToNextMajorVersion;')
lines.append(f'\t\t\t\tminimumVersion = "10.0.0";')
lines.append(f'\t\t\t}};')
lines.append(f'\t\t\trepositorURL = "https://github.com/firebase/firebase-ios-sdk.git";')
lines.append(f'\t\t}};')
lines.append(f'\t\t{GIDSIGNIN_PKG_REF} = {{')
lines.append(f'\t\t\tisa = XCRemoteSwiftPackageReference;')
lines.append(f'\t\t\trequirement = {{')
lines.append(f'\t\t\t\tkind = upToNextMajorVersion;')
lines.append(f'\t\t\t\tminimumVersion = "7.0.0";')
lines.append(f'\t\t\t}};')
lines.append(f'\t\t\trepositorURL = "https://github.com/google/GoogleSignIn-iOS.git";')
lines.append(f'\t\t}};')
lines.append('/* End XCRemoteSwiftPackageReference section */')
lines.append('')

# --- XCSwiftPackageProductDependency ---
lines.append('/* Begin XCSwiftPackageProductDependency section */')
pkg_products = [
    (FIREBASE_CORE_PROD,      FIREBASE_PKG_REF,  'FirebaseCore'),
    (FIREBASE_AUTH_PROD,      FIREBASE_PKG_REF,  'FirebaseAuth'),
    (FIREBASE_FIRESTORE_PROD, FIREBASE_PKG_REF,  'FirebaseFirestore'),
    (FIREBASE_STORAGE_PROD,   FIREBASE_PKG_REF,  'FirebaseStorage'),
    (GIDSIGNIN_PROD,          GIDSIGNIN_PKG_REF, 'GoogleSignIn'),
    (GIDSIGNIN_SWIFT_PROD,    GIDSIGNIN_PKG_REF, 'GoogleSignInSwift'),
]
for prod_id, pkg_ref, product_name in pkg_products:
    lines.append(f'\t\t{prod_id} = {{')
    lines.append(f'\t\t\tisa = XCSwiftPackageProductDependency;')
    lines.append(f'\t\t\tpackage = {pkg_ref};')
    lines.append(f'\t\t\tproductName = {product_name};')
    lines.append(f'\t\t}};')
lines.append('/* End XCSwiftPackageProductDependency section */')
lines.append('')

# --- XCBuildConfiguration ---
def build_config(cfg_id, name, is_target=False):
    lines.append(f'\t\t{cfg_id} = {{')
    lines.append(f'\t\t\tisa = XCBuildConfiguration;')
    lines.append(f'\t\t\tbuildSettings = {{')
    if is_target:
        lines.append(f'\t\t\t\tALWAYS_SEARCH_USER_PATHS = NO;')
        lines.append(f'\t\t\t\tASS ET_CATALOG_COMPILER_OPTIMIZATION = space;')
        lines.append(f'\t\t\t\tCLANG_ANALYZER_NONNULL = YES;')
        lines.append(f'\t\t\t\tCLANG_ANALYZER_NUMBER_OBJECT_CONVERSION = YES_AGGRESSIVE;')
        lines.append(f'\t\t\t\tCODE_SIGN_STYLE = Automatic;')
        lines.append(f'\t\t\t\tCURRENT_PROJECT_VERSION = 1;')
        lines.append(f'\t\t\t\tDEVELOPMENT_ASSET_PATHS = "";')
        lines.append(f'\t\t\t\tENABLE_PREVIEWS = YES;')
        lines.append(f'\t\t\t\tGENERATE_INFOPLIST_FILE = NO;')
        lines.append(f'\t\t\t\tINFOPLIST_FILE = ShelfLife/Resources/Info.plist;')
        lines.append(f'\t\t\t\tIPHONEOS_DEPLOYMENT_TARGET = 16.0;')
        lines.append(f'\t\t\t\tLE_SWIFT_VERSION = 5.0;')
        lines.append(f'\t\t\t\tMARKETING_VERSION = 1.0;')
        lines.append(f'\t\t\t\tPRODUCT_BUNDLE_IDENTIFIER = com.shelflife.app;')
        lines.append(f'\t\t\t\tPRODUCT_NAME = "$(TARGET_NAME)";')
        lines.append(f'\t\t\t\tSWIFT_EMIT_LOC_STRINGS = YES;')
        lines.append(f'\t\t\t\tSWIFT_VERSION = 5.0;')
        lines.append(f'\t\t\t\tTARGETED_DEVICE_FAMILY = "1,2";')
    else:
        lines.append(f'\t\t\t\tALWAYS_SEARCH_USER_PATHS = NO;')
        lines.append(f'\t\t\t\tCLANG_ANALYZER_NONNULL = YES;')
        lines.append(f'\t\t\t\tCOPY_PHASE_STRIP = NO;')
        dbg_fmt = "dwarf-with-dsym" if name == "Release" else "dwarf"
        lines.append(f'\t\t\t\tDEBUG_INFORMATION_FORMAT = {dbg_fmt};')
        lines.append(f'\t\t\t\tENABLE_NS_ASSERTIONS = {"NO" if name == "Release" else "YES"};')
        lines.append(f'\t\t\t\tENABLE_STRICT_OBJC_MSGSEND = YES;')
        lines.append(f'\t\t\t\tIPHONEOS_DEPLOYMENT_TARGET = 16.0;')
        lines.append(f'\t\t\t\tMTL_FAST_MATH = YES;')
        lines.append(f'\t\t\t\tONLY_ACTIVE_ARCH = {"YES" if name == "Debug" else "NO"};')
        lines.append(f'\t\t\t\tSDKROOT = iphoneos;')
        lines.append(f'\t\t\t\tSWIFT_ACTIVE_COMPILATION_CONDITIONS = {"DEBUG" if name == "Debug" else ""};')
        opt = '"-Onone"' if name == "Debug" else '"-O"'
        lines.append(f'\t\t\t\tSWIFT_OPTIMIZATION_LEVEL = {opt};')
        lines.append(f'\t\t\t\tVALIDATE_PRODUCT = {"YES" if name == "Release" else "NO"};')
    lines.append(f'\t\t\t}};')
    lines.append(f'\t\t\tname = {name};')
    lines.append(f'\t\t}};')

lines.append('/* Begin XCBuildConfiguration section */')
build_config(DEBUG_CFG_ID, 'Debug', is_target=False)
build_config(RELEASE_CFG_ID, 'Release', is_target=False)
build_config(DEBUG_TARGET_CFG, 'Debug', is_target=True)
build_config(RELEASE_TARGET_CFG, 'Release', is_target=True)
lines.append('/* End XCBuildConfiguration section */')
lines.append('')

# --- XCConfigurationList ---
lines.append('/* Begin XCConfigurationList section */')
lines.append(f'\t\t{PROJ_CFGLIST_ID} = {{')
lines.append(f'\t\t\tisa = XCConfigurationList;')
lines.append(f'\t\t\tbuildConfigurations = (')
lines.append(f'\t\t\t\t{DEBUG_CFG_ID},')
lines.append(f'\t\t\t\t{RELEASE_CFG_ID},')
lines.append(f'\t\t\t);')
lines.append(f'\t\t\tdefaultConfigurationIsVisible = 0;')
lines.append(f'\t\t\tdefaultConfigurationName = Release;')
lines.append(f'\t\t}};')
lines.append(f'\t\t{TARGET_CFGLIST_ID} = {{')
lines.append(f'\t\t\tisa = XCConfigurationList;')
lines.append(f'\t\t\tbuildConfigurations = (')
lines.append(f'\t\t\t\t{DEBUG_TARGET_CFG},')
lines.append(f'\t\t\t\t{RELEASE_TARGET_CFG},')
lines.append(f'\t\t\t);')
lines.append(f'\t\t\tdefaultConfigurationIsVisible = 0;')
lines.append(f'\t\t\tdefaultConfigurationName = Release;')
lines.append(f'\t\t}};')
lines.append('/* End XCConfigurationList section */')
lines.append('')

lines.append('\t};')
lines.append(f'\trootObject = {PROJECT_ID};')
lines.append('}')

output = '\n'.join(lines)
out_path = os.path.join(os.path.dirname(__file__), 'ShelfLife', 'ShelfLife.xcodeproj', 'project.pbxproj')
os.makedirs(os.path.dirname(out_path), exist_ok=True)
with open(out_path, 'w') as f:
    f.write(output)
print(f'Written {out_path}')

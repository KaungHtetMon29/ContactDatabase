# Contact Database Application - Code Explanations

## Kotlin Files

### 1. MainActivity.kt

**Location:** `app/src/main/java/com/kaunghtetmon/contactdatabase/MainActivity.kt`

**Explanation:**
This is the main entry point activity of the application that handles adding new contacts. It manages a form with profile image selection, name, date of birth, and email input fields. The activity implements permission handling for accessing device storage to select profile images (supporting different Android versions with appropriate permissions). It includes input validation to ensure all fields are filled correctly, checks for valid email format using Android's Patterns class, and prevents duplicate email entries by querying the database. The activity uses View Binding for efficient view access and interacts with ContactViewModel to save contact data to the SQLite database. After successful save, it clears the form and provides user feedback through Snackbar messages. Users can navigate to the contact list view using the "VIEW DETAILS" button.

**Related XML File:** `activity_main.xml`

---

### 2. ContactListActivity.kt

**Location:** `app/src/main/java/com/kaunghtetmon/contactdatabase/ContactListActivity.kt`

**Explanation:**
This activity displays all saved contacts in a scrollable RecyclerView list. It uses the ContactAdapter to bind contact data to individual list items. The activity observes LiveData from ContactViewModel to automatically update the UI whenever contacts are added, modified, or deleted. It implements an empty state view that displays "No contacts yet" when the database is empty. Each contact item has a delete button that shows a confirmation dialog before deletion. Clicking on a contact item navigates to ContactDetailActivity to view full details. The activity includes a back button in the action bar for navigation and refreshes the contact list in onResume() to ensure data is current when returning from detail views.

**Related XML File:** `activity_contact_list.xml`

---

### 3. ContactDetailActivity.kt

**Location:** `app/src/main/java/com/kaunghtetmon/contactdatabase/ContactDetailActivity.kt`

**Explanation:**
This activity serves a dual purpose - displaying contact details and editing contacts. It dynamically switches between two different layouts (detail view and edit view) within the same activity. In detail mode, it shows contact information in a read-only format with material design cards, displaying the profile image, name, email, and date of birth with appropriate icons. Users can click the "Edit Contact" button to switch to edit mode or delete the contact. In edit mode, the layout changes to show editable TextInputFields pre-populated with current data, an image picker for updating the profile photo, and Save/Cancel buttons. The activity implements unsaved changes detection and shows confirmation dialogs when users try to navigate away with unsaved changes. It validates input fields similar to MainActivity and uses the ViewModel to update or delete contacts in the database.

**Related XML Files:** `activity_contact_detail.xml` and `activity_contact_edit.xml`

---

### 4. Contact.kt (Data Class)

**Location:** `app/src/main/java/com/kaunghtetmon/contactdatabase/data/Contact.kt`

**Explanation:**
This is a Kotlin data class that represents the Contact entity/model in the application. It defines the structure of a contact with properties: id (auto-generated primary key), name (person's full name), dob (date of birth as String), email (unique email address), and profileImage (stored as ByteArray for image data). Since it contains a ByteArray which doesn't have default equals/hashCode implementations, the class overrides equals() and hashCode() methods to properly compare contacts including their image data using contentEquals() for the byte array. This ensures correct behavior when contacts are used in collections or compared for equality.

**Related XML Files:** Used across all layout files indirectly through data binding

---

### 5. DatabaseHelper.kt

**Location:** `app/src/main/java/com/kaunghtetmon/contactdatabase/data/DatabaseHelper.kt`

**Explanation:**
This class extends SQLiteOpenHelper and manages all database operations for the application. It creates and maintains a SQLite database named "ContactDatabase.db" with a "contacts" table containing columns for id, name, dob, email, and profile_image (BLOB type for storing image data). The class implements onCreate() to create the initial database schema and onUpgrade() to handle database version changes. It provides CRUD operations: addContact() inserts new contacts, getAllContacts() retrieves all contacts as a list, updateContact() modifies existing contact data, and deleteContact() removes contacts by ID. It includes validation methods isEmailExists() and isEmailExistsExcludingContact() to prevent duplicate email entries. All database operations properly use ContentValues for insertion/updating and Cursors for querying, with proper resource management (closing cursors and database connections).

**Related XML Files:** None directly (backend database layer)

---

### 6. ContactViewModel.kt

**Location:** `app/src/main/java/com/kaunghtetmon/contactdatabase/viewModel/ContactViewModel.kt`

**Explanation:**
This is the ViewModel layer that acts as a bridge between the UI (Activities) and the data layer (DatabaseHelper). It extends AndroidViewModel to access the application context needed for database operations. The class manages a LiveData object (\_allContacts) that holds the current list of contacts, allowing activities to observe and react to data changes automatically. It uses Kotlin Coroutines (viewModelScope.launch) to perform database operations asynchronously on background threads, preventing UI freezing. The ViewModel provides methods matching CRUD operations: insert(), update(), delete(), and refreshContacts(), each handling database operations and updating LiveData. It also exposes email validation methods to check for duplicates. The file also includes ContactViewModelFactory, which is a ViewModelProvider.Factory implementation required to instantiate the ViewModel with the Application context parameter.

**Related XML Files:** None directly (architecture layer)

---

### 7. ContactAdapter.kt

**Location:** `app/src/main/java/com/kaunghtetmon/contactdatabase/adapter/ContactAdapter.kt`

**Explanation:**
This is a RecyclerView adapter that efficiently displays a list of contacts. It extends ListAdapter which automatically handles list diffing for efficient UI updates (calculating minimal changes between old and new lists). The adapter uses ViewBinding (ItemContactBinding) for type-safe view access. It takes two lambda functions as parameters: onItemClick (triggered when a contact is clicked to view details) and onDeleteClick (triggered when the delete button is clicked). The ContactViewHolder inner class binds Contact data to views, displaying name, DOB, email, and profile image (or default icon if no image exists). It implements ContactDiffCallback using DiffUtil.ItemCallback to efficiently compare contacts and determine which items changed, improving RecyclerView performance by only updating modified items instead of refreshing the entire list.

**Related XML File:** `item_contact.xml`

---

### 8. ImageUtils.kt

**Location:** `app/src/main/java/com/kaunghtetmon/contactdatabase/utils/ImageUtils.kt`

**Explanation:**
This is a utility singleton object that provides helper functions for image processing. The compressImage() function takes a Bitmap and reduces its size by resizing to maximum 512x512 pixels (maintaining aspect ratio) and compressing to JPEG format with 50% quality, returning a ByteArray suitable for database storage. This prevents database bloat from large image files. The uriToBitmap() function converts a content URI (from image picker) to a Bitmap, with version-specific handling: using ImageDecoder for Android 9+ and the deprecated MediaStore method for older versions. The byteArrayToBitmap() function converts ByteArray data retrieved from database back into a displayable Bitmap using BitmapFactory. The utility properly handles exceptions and includes memory management considerations by recycling intermediate bitmaps.

**Related XML Files:** Used wherever images are displayed (all layout files with ImageView components)

---

## XML Layout Files

### 1. activity_main.xml

**Location:** `app/src/main/res/layout/activity_main.xml`

**Explanation:**
This is the main screen layout for adding new contacts. It uses a ScrollView as the root to handle content overflow and contains a ConstraintLayout for organized view positioning. The layout includes: a circular CardView (120dp diameter with 60dp corner radius) for profile image display with an overlay camera icon indicating it's clickable; Material Design TextInputLayout components for Name, Date of Birth, and Email fields with proper input types and hints; the DOB field includes a calendar icon and is non-editable (triggers date picker on click); two equal-width buttons at the bottom ("SAVE DETAILS" and "VIEW DETAILS") using ConstraintLayout chains. The design uses Material Design principles with elevation, proper spacing (margins/padding), and white background for clean appearance.

**Used by:** MainActivity.kt

---

### 2. activity_contact_list.xml

**Location:** `app/src/main/res/layout/activity_contact_list.xml`

**Explanation:**
This is a simple but effective list screen layout using ConstraintLayout as the root with a light gray background (#F5F5F5). It contains a RecyclerView that fills the entire screen with clipToPadding set to false for better scrolling appearance with padding. The layout includes an empty state TextView ("No contacts yet") centered in the screen with gray color, initially hidden with visibility="gone" and shown programmatically when the contact list is empty. The RecyclerView is constrained to all parent edges for full-screen display, with 16dp padding for proper item spacing from screen edges.

**Used by:** ContactListActivity.kt

---

### 3. activity_contact_detail.xml

**Location:** `app/src/main/res/layout/activity_contact_detail.xml`

**Explanation:**
This layout displays detailed contact information in a beautiful, read-only card-based design. It uses a ScrollView with a light gray background containing a ConstraintLayout with three main sections: (1) Header Card - contains a large circular profile image (120dp) centered at the top with high elevation (8dp) and the contact's name in large bold text (28sp) below it; (2) Information Card - displays contact details in a structured format with email and date of birth sections, each with an icon (email/calendar), a gray label text, and the actual data in black text, separated by a divider line; (3) Action Buttons - "Edit Contact" button with an edit icon in primary color, and "Delete Contact" button with delete icon in red (#D32F2F). The layout uses Material CardViews with rounded corners (16dp) and elevation for modern design aesthetics.

**Used by:** ContactDetailActivity.kt (detail mode)

---

### 4. activity_contact_edit.xml

**Location:** `app/src/main/res/layout/activity_contact_edit.xml`

**Explanation:**
This layout provides the editing interface for existing contacts, following similar design patterns to the detail view but with editable fields. It contains: (1) Header Card - displays the circular profile image with an "Update Image" outlined button below for changing the photo; (2) Edit Form Card - contains three outlined TextInputLayout fields (Name, Date of Birth, Email) using the Material OutlinedBox style for clear edit mode indication, with proper input types and hints matching the add contact form; (3) Action Buttons - a primary "Save Changes" button with save icon, and an outlined "Cancel" button with close icon below it. The layout maintains consistent spacing, uses white cards on gray background (#F5F5F5), and has high elevation for visual hierarchy. All inputs are pre-populated with existing contact data when the activity switches to edit mode.

**Used by:** ContactDetailActivity.kt (edit mode)

---

### 5. item_contact.xml

**Location:** `app/src/main/res/layout/item_contact.xml`

**Explanation:**
This is the individual list item layout for displaying a single contact in the RecyclerView. It's wrapped in a CardView with rounded corners (8dp), white background, and elevation for material design appearance. The layout uses ConstraintLayout to arrange: (1) a circular profile image (64dp) on the left with either the contact's photo or a default gallery icon; (2) contact information in the middle showing three TextViews stacked vertically - name (16sp, bold, black), DOB (14sp, gray), and email (12sp, gray); (3) a delete button (red trash icon) aligned to the right edge. The card has bottom margin for spacing between items and uses cardUseCompatPadding for consistent appearance across devices. The entire card is clickable for viewing details, while the delete button has its own click handler.

**Used by:** ContactAdapter.kt

---

## XML Resource Files

### 1. AndroidManifest.xml

**Location:** `app/src/main/AndroidManifest.xml`

**Explanation:**
This is the Android manifest file that declares all essential application components and permissions. It requests READ_EXTERNAL_STORAGE permission (for Android 9 and below, with maxSdkVersion="32") and READ_MEDIA_IMAGES permission (for Android 13+) to access device photos. The application tag defines app-wide settings including the app icon, label from strings.xml, Material Components theme (DayNight.DarkActionBar), and backup settings. It declares three activities: MainActivity (the launcher activity with MAIN action and LAUNCHER category), ContactListActivity (with MainActivity as parent for back navigation), and ContactDetailActivity (with ContactListActivity as parent). All non-launcher activities have exported="false" for security, preventing external apps from starting them directly.

---

### 2. strings.xml

**Location:** `app/src/main/res/values/strings.xml`

**Explanation:**
This file contains string resources used throughout the application for internationalization and centralized text management. Currently, it only defines one string resource: app_name with the value "Contact Database", which is used as the application name displayed on the device launcher and in the action bar.

---

### 3. colors.xml

**Location:** `app/src/main/res/values/colors.xml`

**Explanation:**
This file defines the color palette used throughout the application following Material Design color naming conventions. It includes: purple_200, purple_500, and purple_700 for primary brand colors (where 500 is the main primary color and 700 is a darker variant); teal_200 and teal_700 for secondary accent colors; and basic black and white colors. These colors are referenced in the theme and throughout layouts for consistent UI appearance and easy theme customization.

---

### 4. themes.xml

**Location:** `app/src/main/res/values/themes.xml`

**Explanation:**
This file defines the application's visual theme extending Material Components DayNight theme with dark action bar. It sets the color scheme: colorPrimary (purple_500) used for app bar and primary UI elements, colorPrimaryVariant (purple_700) for status bar and darker primary variants, colorOnPrimary (white) for text/icons on primary color, colorSecondary (teal_200) for FABs and accent elements, colorSecondaryVariant (teal_700) for darker secondary variants, and colorOnSecondary (black) for text/icons on secondary color. The theme specifically sets the status bar color to match colorPrimary for unified top appearance. This creates a consistent Material Design look across the entire application.

---

## Application Architecture Summary

The application follows the MVVM (Model-View-ViewModel) architectural pattern with clear separation of concerns:

- **Model Layer:** Contact.kt (data class) and DatabaseHelper.kt (SQLite operations)
- **ViewModel Layer:** ContactViewModel.kt (business logic and data management)
- **View Layer:** Activities (MainActivity, ContactListActivity, ContactDetailActivity) and XML layouts
- **Adapter Layer:** ContactAdapter.kt (RecyclerView list management)
- **Utility Layer:** ImageUtils.kt (helper functions for image processing)

This architecture ensures maintainable, testable code with proper separation between UI, business logic, and data persistence layers.

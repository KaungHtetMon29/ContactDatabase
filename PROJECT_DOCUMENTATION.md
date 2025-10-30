# Contact Database Application - Technical Documentation

## Project Overview

This is an Android mobile application built using Kotlin that allows users to manage a contact database. The application implements CRUD (Create, Read, Update, Delete) operations with a modern Material Design UI using XML layouts and RecyclerView.

## Application Architecture

The application follows the **MVVM (Model-View-ViewModel)** architecture pattern with the following structure:

```
app/
├── data/              # Data models and database management
├── viewModel/         # ViewModel layer for business logic
├── adapter/           # RecyclerView adapters
├── utils/             # Utility classes
├── res/
│   └── layout/        # XML layout files
└── MainActivity.kt & Activities
```

---

## Key Files and Their Purposes

### 1. Data Layer

#### **Contact.kt** (`app/src/main/java/com/kaunghtetmon/contactdatabase/data/Contact.kt`)

**Purpose**: Data model representing a contact entity

**Key Components**:

- `id`: Unique identifier for each contact (Int)
- `name`: Contact's full name (String)
- `dob`: Date of birth in dd/MM/yyyy format (String)
- `email`: Contact's email address (String)
- `profileImage`: Contact's profile photo stored as ByteArray (nullable)

**Why ByteArray for images?**:

- Allows storing images directly in SQLite database
- Efficient for small profile images
- Avoids file system complexity

---

#### **DatabaseHelper.kt** (`app/src/main/java/com/kaunghtetmon/contactdatabase/data/DatabaseHelper.kt`)

**Purpose**: Manages SQLite database operations using Android's SQLiteOpenHelper

**Key Methods**:

1. `onCreate()`: Creates the contacts table when app first launches
2. `addContact()`: Inserts new contact into database
3. `getAllContacts()`: Retrieves all contacts from database
4. `updateContact()`: Updates existing contact information
5. `deleteContact()`: Removes contact from database
6. `isEmailExists()`: Validates email uniqueness for new contacts
7. `isEmailExistsExcludingContact()`: Validates email uniqueness when editing (allows current contact to keep its email)

**Database Schema**:

```sql
CREATE TABLE contacts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    dob TEXT,
    email TEXT,
    profile_image BLOB
)
```

**Why SQLite?**:

- Built into Android platform
- No external dependencies
- Perfect for local data storage
- Lightweight and fast for small datasets

---

### 2. ViewModel Layer

#### **ContactViewModel.kt** (`app/src/main/java/com/kaunghtetmon/contactdatabase/viewModel/ContactViewModel.kt`)

**Purpose**: Manages UI-related data and business logic, survives configuration changes

**Key Features**:

- **LiveData**: Observes data changes and automatically updates UI
- **Coroutines**: Performs database operations asynchronously to avoid blocking UI
- **Lifecycle-aware**: Survives screen rotations and other configuration changes

**Key Methods**:

1. `insert()`: Adds new contact via DatabaseHelper
2. `update()`: Updates existing contact
3. `delete()`: Removes contact
4. `refreshContacts()`: Reloads all contacts from database
5. `isEmailExists()`: Checks email duplication

**Why ViewModel?**:

- Separates business logic from UI
- Survives configuration changes (screen rotation)
- Provides clean architecture
- Makes testing easier

#### **ContactViewModelFactory.kt**

**Purpose**: Factory pattern to create ViewModel with application context dependency

**Why needed?**:

- ViewModel requires Application context for DatabaseHelper
- Android requires factory pattern for ViewModels with constructor parameters

---

### 3. UI Layer - Activities

#### **MainActivity.kt** (`app/src/main/java/com/kaunghtetmon/contactdatabase/MainActivity.kt`)

**Purpose**: Main screen for adding new contacts

**Key Features**:

1. **Image Selection**:

   - Uses ActivityResultContracts for image picking
   - Handles runtime permissions (READ_MEDIA_IMAGES for Android 13+)
   - Compresses images to reduce database size

2. **Date Picker**:

   - DatePickerDialog for birth date selection
   - Age validation (minimum 16 years old)
   - Format: dd/MM/yyyy

3. **Form Validation**:

   - Name: Cannot be empty
   - Date of Birth: Cannot be empty, must be 16+ years old
   - Email: Cannot be empty, must be valid format, must be unique

4. **ViewBinding**:
   - Type-safe way to access XML layout views
   - Replaces findViewById()
   - Compile-time safety

**Image Handling Process**:

```
1. User selects image from gallery
2. Image converted to Bitmap
3. Bitmap compressed to JPEG (quality 50%, max 512px)
4. Compressed data stored as ByteArray
5. Original bitmap recycled to free memory
```

---

#### **ContactListActivity.kt** (`app/src/main/java/com/kaunghtetmon/contactdatabase/ContactListActivity.kt`)

**Purpose**: Displays all contacts in a scrollable list

**Key Features**:

1. **RecyclerView**:

   - Efficiently displays large lists
   - Reuses view holders for better performance
   - Smooth scrolling

2. **LiveData Observer**:

   - Automatically updates list when data changes
   - No manual refresh needed after CRUD operations

3. **Empty State**:

   - Shows "No contacts yet" when list is empty
   - Better user experience

4. **Delete from List**:

   - Quick delete with confirmation dialog
   - Shows success/failure feedback via Snackbar

5. **onResume() Refresh**:
   - Reloads data when returning from detail screen
   - Ensures list always shows latest data

**Why RecyclerView over ListView?**:

- More flexible and efficient
- Better performance with large datasets
- Supports various layout managers
- Modern Android standard

---

#### **ContactDetailActivity.kt** (`app/src/main/java/com/kaunghtetmon/contactdatabase/ContactDetailActivity.kt`)

**Purpose**: Shows detailed contact information and provides edit/delete functionality

**Key Features**:

1. **Dual Mode Design**:

   - **View Mode**: Read-only display of contact details
   - **Edit Mode**: Editable form to update contact

2. **Dynamic Layout Switching**:

   - Uses two separate layouts (detail and edit)
   - Switches between them using setContentView()
   - Preserves data during mode changes

3. **Unsaved Changes Detection**:

   - Compares current form data with original values
   - Only shows "Discard Changes?" dialog if data modified
   - Prevents annoying prompts when no changes made

4. **Change Detection Logic**:

   ```kotlin
   - Compare name, DOB, email (string comparison)
   - Compare image (byte array comparison using contentEquals())
   - Return true if any field differs
   ```

5. **Image Update**:
   - Allows changing profile photo while editing
   - Same compression logic as adding new contact

**Layout Files Used**:

- `activity_contact_detail.xml`: View mode (read-only)
- `activity_contact_edit.xml`: Edit mode (editable fields)

---

### 4. Adapter Layer

#### **ContactAdapter.kt** (`app/src/main/java/com/kaunghtetmon/contactdatabase/adapter/ContactAdapter.kt`)

**Purpose**: Adapter for RecyclerView to display contact items

**Key Components**:

1. **ListAdapter**:

   - Automatically calculates differences between lists
   - Smooth animations when data changes
   - More efficient than regular RecyclerView.Adapter

2. **DiffUtil.ItemCallback**:

   - Compares old and new contact items
   - Determines which items changed
   - Enables efficient list updates

3. **ViewHolder Pattern**:

   - Caches view references for better performance
   - Binds data to views efficiently
   - Reuses views as user scrolls

4. **Click Listeners**:
   - `onItemClick`: Opens contact detail screen
   - `onDeleteClick`: Triggers delete confirmation

**Binding Process**:

```kotlin
1. ViewHolder created with ItemContactBinding
2. Contact data passed to bind() method
3. Name, DOB, email set to TextViews
4. Profile image loaded from ByteArray
5. Click listeners attached
```

---

### 5. Utility Classes

#### **ImageUtils.kt** (`app/src/main/java/com/kaunghtetmon/contactdatabase/utils/ImageUtils.kt`)

**Purpose**: Helper functions for image processing

**Key Functions**:

1. **compressImage()**:

   - Resizes images to max 512x512 pixels
   - Compresses to JPEG with 50% quality
   - Reduces database size significantly
   - Returns ByteArray for database storage

2. **uriToBitmap()**:

   - Converts gallery image URI to Bitmap
   - Handles different Android versions
   - Uses ImageDecoder for Android 9+ (modern)
   - Falls back to MediaStore for older versions

3. **byteArrayToBitmap()**:
   - Converts stored ByteArray back to Bitmap
   - Used when displaying saved images
   - Handles null/empty arrays safely

**Why Image Compression?**:

- Original photos can be 5-10 MB
- Compressed images are ~50-100 KB
- Faster database operations
- Less storage space used
- Profile pictures don't need full resolution

---

### 6. Layout Files

#### **activity_main.xml**

**Purpose**: Layout for adding new contacts

**Key UI Components**:

- Profile image picker (circular CardView)
- Name input (TextInputLayout with EditText)
- Date of Birth input (non-editable, opens DatePicker)
- Email input (TextInputLayout with email validation)
- Save button
- View Details button

**Material Design Elements**:

- MaterialCardView for elevation and rounded corners
- TextInputLayout for floating labels and error messages
- Outlined box style for modern look
- ConstraintLayout for flexible positioning

---

#### **activity_contact_list.xml**

**Purpose**: Layout for contact list screen

**Components**:

- RecyclerView for scrollable contact list
- Empty state TextView (shown when no contacts)
- Background color: Light gray (#F5F5F5) for contrast

---

#### **item_contact.xml**

**Purpose**: Layout for individual contact items in RecyclerView

**Components**:

- Circular profile image (64dp)
- Name (bold, 16sp)
- Date of Birth (14sp, gray)
- Email (12sp, gray)
- Delete button (ImageButton, right-aligned)

**Design Features**:

- CardView with elevation for depth
- Rounded corners (8dp)
- Padding for breathing room
- Delete icon in red (#D32F2F)

---

#### **activity_contact_detail.xml**

**Purpose**: Layout for viewing contact details (read-only mode)

**Components**:

- Large circular profile image (120dp) at top
- Name displayed prominently (28sp, bold, centered)
- Information card with email and DOB
- Icons for each field (email icon, calendar icon)
- Edit button (primary color)
- Delete button (red, destructive action)

**Visual Hierarchy**:

- Profile image is focal point
- Name is prominent
- Information organized in card
- Actions at bottom

---

#### **activity_contact_edit.xml**

**Purpose**: Layout for editing contact details

**Components**:

- Profile image (clickable for update)
- "Update Image" button below profile
- Editable input fields (Name, DOB, Email)
- Save Changes button (primary)
- Cancel button (outlined style)

**Differences from Detail View**:

- Fields are editable
- Update Image button instead of camera overlay
- Save/Cancel buttons instead of Edit/Delete

---

### 7. Android Manifest

#### **AndroidManifest.xml**

**Purpose**: Declares app components and permissions

**Key Declarations**:

1. **Permissions**:

   ```xml
   - READ_MEDIA_IMAGES (Android 13+)
   - READ_EXTERNAL_STORAGE (Android 12 and below)
   ```

2. **Activities**:

   - MainActivity (launcher activity)
   - ContactListActivity (with back navigation)
   - ContactDetailActivity (with back navigation)

3. **Theme**: MaterialComponents.DayNight.DarkActionBar

4. **Parent Activity Navigation**:
   - Enables up button in action bar
   - Provides proper back stack navigation

---

### 8. Gradle Build Files

#### **build.gradle.kts (app level)**

**Purpose**: Defines app dependencies and build configuration

**Key Dependencies**:

1. **AndroidX Core Libraries**:

   - `core-ktx`: Kotlin extensions
   - `appcompat`: Backward compatibility
   - `lifecycle`: ViewModel and LiveData

2. **UI Libraries**:

   - `material`: Material Design components
   - `constraintlayout`: Flexible layouts
   - `recyclerview`: Efficient lists
   - `cardview`: Card-based layouts

3. **ViewBinding**:

   - Enabled in buildFeatures
   - Type-safe view access

4. **Kotlin**:
   - Primary language
   - Coroutines for async operations

---

## Key Technologies & Concepts

### 1. **MVVM Architecture**

- **Model**: Contact data class and DatabaseHelper
- **View**: Activities and XML layouts
- **ViewModel**: ContactViewModel managing data and business logic

**Benefits**:

- Separation of concerns
- Testable code
- Lifecycle awareness
- Maintainable structure

---

### 2. **LiveData**

Observable data holder that respects Android lifecycle

**Advantages**:

- Automatic UI updates
- No memory leaks
- No crashes on stopped activities
- Lifecycle-aware

**Usage in App**:

```kotlin
contactViewModel.allContacts.observe(this) { contacts ->
    // UI automatically updates when data changes
    contactAdapter.submitList(contacts)
}
```

---

### 3. **ViewBinding**

Type-safe way to access views

**Before (findViewById)**:

```kotlin
val button = findViewById<Button>(R.id.saveButton)
```

**After (ViewBinding)**:

```kotlin
binding.saveButton.setOnClickListener { }
```

**Benefits**:

- Null safety
- Type safety
- No runtime crashes from wrong IDs

---

### 4. **Material Design**

Google's design system for Android

**Components Used**:

- MaterialCardView
- TextInputLayout
- Snackbar (instead of Toast)
- MaterialButton
- Elevation and shadows
- Color schemes

---

### 5. **Image Handling**

**Compression Strategy**:

- Max dimension: 512px
- Format: JPEG
- Quality: 50%
- Result: ~50-100 KB per image

**Storage**: ByteArray in SQLite BLOB column

---

### 6. **Permissions Handling**

**Modern Approach**:

- Uses ActivityResultContracts
- Runtime permission requests
- Version-specific permissions (Android 13+)
- Graceful fallback for denied permissions

---

### 7. **RecyclerView with ListAdapter**

**Efficiency Features**:

- View recycling (reuses view holders)
- DiffUtil for smart updates
- Smooth animations
- Handles large datasets efficiently

---

## Application Features

### ✅ Implemented Features

1. **Add Contact**

   - Profile image selection
   - Age validation (16+ years)
   - Email validation and uniqueness check
   - Form validation with error messages

2. **View Contact List**

   - Scrollable list with RecyclerView
   - Empty state when no contacts
   - Automatic refresh on data changes
   - Quick delete from list

3. **View Contact Details**

   - Full contact information display
   - Large profile image
   - Organized information cards

4. **Edit Contact**

   - Update all fields including image
   - Age validation maintained
   - Email uniqueness check (excluding current contact)
   - Unsaved changes detection
   - Confirmation dialog on discard

5. **Delete Contact**

   - Delete from detail view
   - Delete from list view
   - Confirmation dialog
   - Success/failure feedback

6. **Data Persistence**

   - SQLite database storage
   - Survives app restarts
   - CRUD operations

7. **UI/UX Enhancements**
   - Material Design
   - Snackbar notifications (no app logo)
   - Loading states
   - Error handling
   - Card shadows and elevation

---

## Data Flow

### Adding a Contact:

```
1. User fills form in MainActivity
2. Validates all fields
3. Checks email uniqueness
4. Compresses profile image
5. Creates Contact object
6. ContactViewModel.insert() called
7. DatabaseHelper.addContact() saves to SQLite
8. LiveData updates automatically
9. Success message shown
10. Form cleared
```

### Editing a Contact:

```
1. User clicks contact in list
2. ContactDetailActivity opens with data
3. User clicks "Edit Contact"
4. Layout switches to edit mode
5. User modifies fields
6. Clicks "Save Changes"
7. Validates modified data
8. ContactViewModel.update() called
9. DatabaseHelper.updateContact() updates SQLite
10. Returns to detail view with updated data
11. List refreshes on return (onResume)
```

### Deleting a Contact:

```
1. User clicks delete button
2. Confirmation dialog shown
3. User confirms
4. ContactViewModel.delete() called
5. DatabaseHelper.deleteContact() removes from SQLite
6. LiveData updates list automatically
7. Success message shown
8. Activity closes if in detail view
```

---

## Best Practices Implemented

1. **Separation of Concerns**: Clear separation between data, business logic, and UI
2. **Single Responsibility**: Each class has one clear purpose
3. **Error Handling**: Try-catch blocks for critical operations
4. **Resource Management**: Proper bitmap recycling to prevent memory leaks
5. **User Feedback**: Clear messages for all actions
6. **Input Validation**: All user inputs validated
7. **Lifecycle Awareness**: Proper handling of Android lifecycle
8. **Modern APIs**: Using latest Android APIs and best practices
9. **Type Safety**: Kotlin and ViewBinding for compile-time safety
10. **Memory Efficiency**: Image compression and proper cleanup

---

## Conclusion

This Contact Database application demonstrates a comprehensive understanding of:

- Android app development fundamentals
- MVVM architecture pattern
- SQLite database management
- Material Design implementation
- Modern Android development practices
- Kotlin programming language
- Lifecycle-aware components
- Efficient list handling with RecyclerView

The application provides a complete CRUD experience with a clean, intuitive user interface following Material Design guidelines and Android development best practices.

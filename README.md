# OCR POC Android App

A proof-of-concept Android application that uses OCR (Optical Character Recognition) to extract text from images using Google ML Kit.

## Features

- **Camera Integration**: Take photos directly with the device camera
- **Gallery Selection**: Choose existing images from the device gallery
- **OCR Text Extraction**: Extract text from images using Google ML Kit Text Recognition
- **Text Display**: View extracted text in a scrollable text view
- **Copy to Clipboard**: Copy extracted text to clipboard for further use
- **Modern UI**: Clean Material Design interface with progress indicators

## Technical Implementation

### Dependencies Used

- **Google ML Kit Text Recognition**: For accurate text recognition
- **CameraX**: For camera functionality
- **Glide**: For image loading and display
- **Kotlin Coroutines**: For asynchronous operations
- **View Binding**: For type-safe view references

### Key Components

1. **MainActivity**:
   - Handles camera and gallery image selection
   - Manages permissions for camera and storage access
   - Processes images and extracts text using ML Kit
   - Displays results and handles user interactions

2. **OCR Engine**:
   - Uses Google ML Kit Text Recognition for high accuracy
   - Processes images asynchronously to avoid blocking the UI
   - Supports multiple languages and scripts
   - Works offline after initial model download

3. **Image Processing**:
   - Loads images from camera or gallery
   - Converts to appropriate format for ML Kit processing
   - Handles different image orientations and formats

## Setup Instructions

1. **Clone the repository**:
   ```bash
git clone https://github.com/dyerelian/ocr-android-poc.git
cd ocr-android-poc
   ```

2. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the project directory and select it

3. **Build and Run**:
   - Connect an Android device or start an emulator
   - Click the "Run" button in Android Studio
   - Grant camera and storage permissions when prompted

## Usage

1. **Launch the app** on your Android device
2. **Take a photo** using the camera button or **select an image** from gallery
3. **Tap "Process Image"** to extract text from the selected image
4. **View the extracted text** in the text area below
5. **Copy the text** to clipboard using the copy button

## Permissions

The app requires the following permissions:
- `CAMERA`: To capture photos
- `READ_EXTERNAL_STORAGE`: To access images from gallery

## Requirements

- Android 5.0 (API level 21) or higher
- Camera hardware
- Storage access
- Internet connection (for initial ML Kit model download)

## Troubleshooting

- **Permission Issues**: Make sure to grant camera and storage permissions
- **Poor OCR Results**: Ensure images are well-lit and text is clearly visible
- **Build Errors**: Make sure all dependencies are properly synced in Android Studio
- **Model Download**: First-time usage may require internet connection for ML Kit model download

## Advantages of Google ML Kit

- **High Accuracy**: Better text recognition compared to traditional OCR libraries
- **Multiple Languages**: Supports 50+ languages out of the box
- **Offline Capability**: Works without internet after initial setup
- **Easy Integration**: Simple API with minimal configuration
- **Regular Updates**: Google maintains and improves the models

## Future Enhancements

- Support for handwritten text recognition
- Batch processing of multiple images
- Text editing capabilities
- Export functionality (PDF, text files)
- Custom model training for specific use cases

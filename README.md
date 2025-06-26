# Aisisstant – Smart Task Assistant (Android App)

Aisisstant is a smart, offline Android app that helps you stay organized by understanding and managing your tasks written in natural language.  
It uses two custom-trained ONNX models to:

* Automatically **categorize** tasks (e.g., “Pay bill” → Finance)
* **Detect dates, times, and recurrence patterns** (e.g., “every Friday at 6 PM”)

> Everything runs directly on your device with no internet needed, thanks to [ONNX Runtime](https://onnxruntime.ai/).

---

## Key Features

* Works entirely offline with no server or internet required
* Real-time task categorization using a fine-tuned BERT model
* Named Entity Recognition (NER) for understanding time, dates, and repetitions
* All models and tokenizers are optimized for Android compatibility
* Includes a basic task scheduling engine, ready for notifications
* Lightweight and fast to run on most devices

---

## Folder Structure

Your model files should go in the following folder:

app/src/main/assets/
├── category_model.onnx          # For task categorization
├── recurrence_model.onnx        # For recurrence/date/time detection
├── tokenizer_config.json
├── vocab.txt
├── special_tokens_map.json
├── tokenizer.json
├── id2label.json


These files are not included in the repository to keep it clean and lightweight.

---

## Download the Models

All model files are available for download from Google Drive:

| Model Files | [Download from Drive](https://drive.google.com/drive/folders/17w0NqOnExI3697M48bElHCLwEJHmw4LH?usp=sharing) |

**After downloading:**

1. Extract the .zip files
2. Move everything into app/src/main/assets/

---

## Getting Started

1. **Clone the repository**

bash
git clone https://github.com/MShaheerMalik77/AisisstantApp.git


2. **Open the project in Android Studio**

3. **Add the model files**  
   Place .onnx, .json, and related files into app/src/main/assets/

4. **Run the app** on your device or emulator

---

## Android Permissions

For Android 13+:

kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
}


---

## Built With

* [ONNX Runtime for Android](https://github.com/microsoft/onnxruntime)
* Hugging Face Transformers
* BERT models for both classification and token-level NER tasks

---

## Created By

**Muhammad Shaheer Malik**  
FAST-NUCES Lahore  
[LinkedIn](https://www.linkedin.com/in/muhammad-shaheer-malik-79b731340/) • [GitHub](https://github.com/mshaheermalik77)

---
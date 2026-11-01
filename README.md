# TextFlow

### Speak Naturally. Write Freely.

TextFlow is an Android voice-to-text application that makes writing messages easier and more convenient. Instead of switching between apps or typing everything manually, you can use a floating microphone window to speak and convert your voice into text.

The idea is simple: open WhatsApp, Instagram, Telegram, or any other app, tap the floating TextFlow icon, speak your message, copy the generated text, and paste it wherever you need.

---

## Features

- **Floating Overlay:** Access TextFlow without leaving your current application.
- **Voice-to-Text:** Convert spoken words into written text.
- **Mini Floating Window:** Use voice input through a compact interface.
- **Text Processing:** Review and edit recognized text before copying.
- **Quick Copy:** Copy your final text directly to the clipboard.
- **Easy to Use:** A minimal interface designed for everyday use.
- **Android Support:** Designed to work on Android 8.0 and above.

## How It Works

1. Open TextFlow on your Android device.
2. Grant the required permissions.
3. Enable the floating overlay.
4. Open any application where you want to type.
5. Tap the floating TextFlow icon.
6. Speak your message.
7. Review the recognized text.
8. Copy it and paste it into your conversation.

## Screenshots

<!-- Add your application screenshots here -->

<!-- Example:
![Home Screen](screenshots/home.png)
![Floating Window](screenshots/overlay.png)
-->

## Download

You can download the latest version of TextFlow from the GitHub Releases page.

**[Download Latest Release](../../releases/latest)**

Download the APK, install it on your Android device, and grant the necessary permissions when prompted.

## Requirements

- Android 8.0 (Oreo) or later.
- Internet connection for online speech recognition where required.
- Microphone permission.
- Display over other apps permission.

## Permissions

TextFlow requires certain Android permissions to provide its features.

| Permission | Purpose |
|---|---|
| Microphone | Capture your voice for transcription. |
| Internet | Connect to the available online speech recognition service. |
| Display over other apps | Show the floating TextFlow window. |
| Notifications | Display foreground service notifications where required. |

TextFlow should only use the microphone when you explicitly start voice input.

## Technology Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Database:** Room
- **Preferences:** DataStore
- **Voice Recognition:** Android SpeechRecognizer
- **Overlay:** Android WindowManager
- **Platform:** Android

## Compatibility

TextFlow is designed for Android 8.0 and newer versions, with particular attention to Android 12 compatibility.

Some functionality may vary depending on the Android version, device manufacturer, and installed speech recognition services.

## Limitations

- Speech recognition depends on the recognition service available on your device.
- An internet connection may be required for voice recognition.
- Recognition accuracy can vary depending on pronunciation, accent, and background noise.
- TextFlow does not automatically send messages to other applications.
- Floating overlay functionality requires the appropriate Android permission.

## Privacy

TextFlow is designed to keep the voice input process under the user's control.

- Microphone access requires permission.
- Recording starts through user interaction.
- The application does not automatically send messages.
- Users control when to copy and share their generated text.

For more information, check the application's source code and permission declarations.

## Contributing

Contributions, suggestions, and bug reports are welcome.

If you want to contribute:

1. Fork this repository.
2. Create a new branch.
3. Make your changes.
4. Test your changes.
5. Submit a Pull Request.

For bug reports or feature suggestions, please open an issue.

## Roadmap

Some ideas for future improvements:

- [ ] Improve speech recognition accuracy.
- [ ] Better Hindi and Hinglish support.
- [ ] More language options.
- [ ] Improved floating window customization.
- [ ] Better text formatting options.
- [ ] Additional accessibility improvements.
- [ ] Performance and battery optimizations.

## License

This project is distributed under the license specified in the repository. Please check the `LICENSE` file for details.

---

## A Note from the Developer

TextFlow started with a simple idea: typing shouldn't always be necessary when you can just speak.

The goal is to make everyday communication easier, especially for people who prefer speaking over typing.

This is the beginning of the project, and there's still plenty of room to improve.

Thanks for checking out TextFlow!

**Made with care for Android.**

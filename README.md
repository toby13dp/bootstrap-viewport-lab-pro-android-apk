# Bootstrap Viewport Lab Pro — Android APK project

Dit is een echte Android-app voor **Bootstrap Viewport Lab Pro**.

De app:

- gebruikt je eigen app-icoon
- start intern een lokale webserver
- gebruikt vaste poort **53847**
- opent de tool in een WebView
- draait lokaal op Android via `http://127.0.0.1:53847`
- bevat alle HTML/CSS/JS-assets offline in de APK

## Appgegevens

```txt
Appnaam: Viewport Lab Pro
Package: be.creatieplezier.viewportlab
Localhost: http://127.0.0.1:53847
Poort: 53847
Min SDK: 23
Target SDK: 35
Compile SDK: 35
```

## Builden in Android Studio

1. Pak de ZIP uit.
2. Open Android Studio.
3. Kies **Open**.
4. Selecteer deze projectmap.
5. Wacht tot Gradle Sync klaar is.
6. Kies **Build > Build APK(s)**.
7. De APK staat daarna normaal hier:

```txt
app/build/outputs/apk/debug/app-debug.apk
```

## Builden met PowerShell

```powershell
./build-debug-apk.ps1
```

## Builden met Gradle

```sh
gradle assembleDebug
```

## Interne werking

De Android Activity start deze server:

```txt
127.0.0.1:53847
```

De WebView opent daarna:

```txt
http://127.0.0.1:53847/
```

Alle webbestanden zitten in:

```txt
app/src/main/assets/www/
```

## Opmerking

Deze projectmap is compileerbaar in Android Studio. In deze ChatGPT-sandbox is geen Android SDK/Gradle-buildomgeving beschikbaar, daarom zit hier de volledige APK-projectmap met bouwscripts in plaats van een reeds gecompileerde APK.

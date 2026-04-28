# Online APK compileren met GitHub Actions

Deze projectversie bevat al een workflow:

```txt
.github/workflows/build-debug-apk.yml
```

## Stappen

1. Maak een nieuwe GitHub repository.
2. Upload de inhoud van deze projectmap naar de repository.
3. Ga in GitHub naar **Actions**.
4. Kies **Build Android Debug APK**.
5. Klik op **Run workflow**.
6. Wacht tot de build klaar is.
7. Open de workflow-run.
8. Download het artifact:

```txt
ViewportLabPro-debug-apk
```

Daarin zit normaal:

```txt
app-debug.apk
```

## Automatisch bouwen

De workflow draait ook automatisch wanneer je pusht naar:

```txt
main
master
```

## Buildgegevens

```txt
Java: 17
Android SDK: android-35
Build tools: 35.0.0
Gradle: 8.9
Gradle task: assembleDebug
```

## APK installeren

Download `app-debug.apk` op je Android en open het bestand.
Je moet mogelijk **Installeren uit onbekende bronnen** toestaan.

# Risparmio

App Android nativa per la gestione delle finanze personali, scritta in **Kotlin** con **Jetpack Compose** (Material 3).

## Funzionalita'

- **Calendario** - Visualizzazione mensile con indicatori spese/entrate e colori semaforo budget
- **Dashboard** - Bilancio mensile, budget giornaliero, gestione entrate/uscite fisse, ultimi movimenti
- **Statistiche** - Donut chart animato, dettaglio per categoria, andamento 6 mesi
- **Quick-Add** - Aggiunta rapida spese/entrate dal calendario tramite bottom sheet
- **Export PDF** - Report mensile esportabile in formato PDF
- **Condivisione APK** - Condividi l'app direttamente dal menu impostazioni

## Architettura

- **UI**: Jetpack Compose + Material 3
- **Database**: Room (SQLite)
- **Pattern**: MVVM con ViewModel + StateFlow
- **Navigazione**: Navigation Compose con Bottom Navigation Bar
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34

## Struttura progetto

```
android/app/src/main/kotlin/com/youandmedia/risparmio/
├── MainActivity.kt
├── RisparmioApp.kt
├── data/
│   ├── AppDatabase.kt
│   ├── dao/ (ExpenseDao, IncomeDao, FixedIncomeDao, FixedExpenseDao)
│   └── model/ (Expense, Income, FixedIncome, FixedExpense)
├── navigation/
│   └── NavGraph.kt
├── ui/
│   ├── calendar/ (CalendarScreen, CalendarViewModel)
│   ├── dashboard/ (DashboardScreen, DashboardViewModel)
│   ├── daydetail/ (DayDetailScreen, DayDetailViewModel)
│   ├── settings/ (SettingsScreen)
│   ├── splash/ (SplashScreen)
│   ├── stats/ (StatsScreen, StatsViewModel)
│   ├── components/ (GaugeWidget, Dialogs)
│   └── theme/ (Theme.kt)
└── util/
    └── Constants.kt
```

## Requisiti

- **Android Studio** Hedgehog (2023.1.1) o superiore
- **JDK** 17
- **Android SDK** 34
- **Gradle** 8.4

## Compilazione

### Da Android Studio

1. Apri la cartella `android/` come progetto Android Studio
2. Attendi la sincronizzazione Gradle
3. Clicca **Build > Build Bundle(s) / APK(s) > Build APK(s)**
4. L'APK si trova in `android/app/build/outputs/apk/debug/app-debug.apk`

### Da linea di comando

```bash
cd android

# Build debug APK
./gradlew assembleDebug

# L'APK viene generato in:
# app/build/outputs/apk/debug/app-debug.apk
```

Su Windows usa `gradlew.bat` al posto di `./gradlew`.

## Installazione sul dispositivo

### Da Android Studio

1. Collega il dispositivo via USB (con debug USB abilitato)
2. Seleziona il dispositivo dal menu a tendina
3. Clicca **Run** (triangolo verde) o premi `Shift+F10`

### Da linea di comando

```bash
# Compila e installa direttamente
cd android
./gradlew installDebug

# Oppure installa un APK gia' compilato
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Abilitare il debug USB sul dispositivo

1. Vai in **Impostazioni > Info telefono**
2. Tocca 7 volte **Numero build** per abilitare le opzioni sviluppatore
3. Vai in **Impostazioni > Opzioni sviluppatore**
4. Abilita **Debug USB**

## Build release

```bash
cd android

# Genera APK release (richiede signing config)
./gradlew assembleRelease
```

Per firmare l'APK di release, configura il keystore in `android/app/build.gradle.kts`.

## Sviluppatore

**You&Media** - youandmedia.it

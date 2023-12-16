import 'package:flutter/material.dart';
import 'package:risparmio/splashscreen.dart';
//import 'package:intl/date_symbol_data_file.dart';
import 'package:table_calendar/table_calendar.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:path_provider/path_provider.dart';
import 'package:risparmio/daymanagement.dart';
//import 'package:risparmio/models/hive_spese.g.dart';
import 'package:risparmio/models/hive.dart';
import 'package:risparmio/models/constants.dart';

import 'package:intl/date_symbol_data_local.dart';
import 'package:risparmio/mainDrawer.dart';

import 'package:risparmio/widget/gauge.dart';
//import 'dart:developer';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await initializeDateFormatting('it_IT');
  //final appDocumentDir = await getApplicationDocumentsDirectory();
  await HiveManager().initializeHive();
  runApp(const MyApp());
}

bool isSameDay(DateTime date1, DateTime date2) {
  return date1.year == date2.year &&
      date1.month == date2.month &&
      date1.day == date2.day;
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Calendar App',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: SplashScreen(),
    );
  }
}

class CalendarScreen extends StatefulWidget {
  const CalendarScreen({super.key});

  @override
  // ignore: library_private_types_in_public_api
  _CalendarScreenState createState() => _CalendarScreenState();
}

class _CalendarScreenState extends State<CalendarScreen>
    with TickerProviderStateMixin {
  final CalendarFormat _calendarFormat = CalendarFormat.month;

  DateTime _meseSelezionato = DateTime.now();
  final DateTime _selectedDay = DateTime.now();
  //DateTime _focusedDay = DateTime.now();

  List<DateTime> datesWithExpenseIcon = [];
  List<DateTime> datesWithIncomeIcon = [];

  List<FixedIncome> fixedIncomes = [];
  List<FixedExpense> fixedExpenses = [];

  double totalFixedIncomes = 0.0;
  double totalFixedExpenses = 0.0;
  double dailyBudget = 0.0;

  late AnimationController controller;

  // Per calcolare le spese totali
  double totalExpensesOfMonth = 0.0;

  // Per Calcolare il FOrecast di spesa
  double forecastExpensesOfMonth = 0.0;

  @override
  void initState() {
    super.initState();
    loadDatesWithExpenses();
    loadDatesWithIncomes();
    calculateFinancialStats();
    setState(() {});

    controller = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 2),
    )..addListener(() {
        setState(() {});
      });
    //controller.repeat(reverse: false);
    controller.forward();

    super.initState();
  }

  @override
  void dispose() {
    controller.dispose();
    super.dispose();
  }

  void _onMonthChanged(DateTime focusedDay) {
    setState(() {
      _meseSelezionato = DateTime(focusedDay.year, focusedDay.month);
      loadDatesWithExpenses();
      loadDatesWithIncomes();
      calculateFinancialStats();
    });
  }

  void loadDatesWithExpenses() async {
    datesWithExpenseIcon =
        await HiveManager().getDatesWithExpenses(_meseSelezionato);
    setState(() {}); // Aggiorna lo stato per riflettere i nuovi dati
  }

  void loadDatesWithIncomes() async {
    datesWithIncomeIcon =
        await HiveManager().getDatesWithIncomes(_meseSelezionato);
    setState(() {}); // Aggiorna lo stato per riflettere i nuovi dati
  }

  void calculateFinancialStats() async {
    // Per calcolare le spese totali
    totalExpensesOfMonth =
        HiveManager().calculateTotalExpensesOfMonth(_meseSelezionato);

    // Per Calcolare il FOrecast di spesa
    forecastExpensesOfMonth =
        HiveManager().calculateForecastOfMonth(_meseSelezionato);

    final fixedIncomes = await HiveManager().getFixedIncomes();
    final fixedExpenses = await HiveManager().getFixedExpenses();

    totalFixedIncomes =
        fixedIncomes.fold(0.0, (sum, item) => sum + item.amount);
    totalFixedExpenses =
        fixedExpenses.fold(0.0, (sum, item) => sum + item.amount);

    final difference = totalFixedIncomes - totalFixedExpenses;
    final daysInMonth =
        DateTime(DateTime.now().year, DateTime.now().month + 1, 0).day;
    dailyBudget = difference / daysInMonth;

    setState(() {});
  }

  late Box<Expense> expenseBox;
  late Box<Income> incomeBox;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        drawer: MainDrawer(),
        appBar: AppBar(
          title: const Text('Risparmio Mensile'),
        ),
        backgroundColor: bgColor,
        body: SingleChildScrollView(
            child: Column(children: [
          Padding(
              padding: const EdgeInsets.all(10.0),
              child: Container(
                  decoration: BoxDecoration(
                      color: Colors.white, // Sfondo rosso intenso
                      borderRadius: BorderRadius.circular(30.0)),
                  child: TableCalendar(
                    locale: 'it_IT',
                    headerVisible: false,
                    firstDay: DateTime.utc(2010, 1, 1),
                    lastDay: DateTime.utc(2030, 12, 31),
                    daysOfWeekStyle: const DaysOfWeekStyle(
                        weekdayStyle: TextStyle(
                            fontSize: 15, fontWeight: FontWeight.bold),
                        weekendStyle: TextStyle(
                            fontSize: 12,
                            color: Colors.redAccent,
                            fontWeight: FontWeight.bold)),
                    daysOfWeekHeight: 28,
                    calendarStyle: const CalendarStyle(
                      defaultTextStyle:
                          TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
                      weekendTextStyle: TextStyle(
                          fontSize: 42,
                          color: Colors.redAccent,
                          fontWeight: FontWeight.bold),
                      todayDecoration: BoxDecoration(
                        color: Colors
                            .blueAccent, // Sostituisci con il tuo colore preferito
                        shape: BoxShape.rectangle,
                      ),
                    ),
                    startingDayOfWeek: StartingDayOfWeek.monday,
                    //focusedDay: _focusedDay,

                    calendarFormat: _calendarFormat,
                    selectedDayPredicate: (day) {
                      return isSameDay(_selectedDay, day);
                    },
                    onDaySelected: (selectedDay, focusedDay) {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                            builder: (context) =>
                                ExpenseDetailsScreen(selectedDay: selectedDay)),
                      );
                    },
                    focusedDay: _meseSelezionato,
                    onPageChanged: (focusedDay) {
                      _onMonthChanged(focusedDay);
                    },
                    calendarBuilders: CalendarBuilders(
                      defaultBuilder: (context, day, focusedDay) {
                        final isWeekend = day.weekday == DateTime.saturday ||
                            day.weekday == DateTime.sunday;
                        final isToday = isSameDay(day, DateTime.now());

                        final TextStyle dayStyle;
                        if (isToday) {
                          dayStyle = const TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                              color: Colors.blue);
                        } else if (isWeekend) {
                          dayStyle = const TextStyle(
                              fontSize: 18,
                              color: Colors.redAccent,
                              fontWeight: FontWeight.bold);
                        } else {
                          dayStyle = const TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                              color: Colors.black);
                        }
                        // RestColor.fromARGB(137, 1, 0, 0)get che combina il numero del giorno e l'icona
                        return Stack(
                          children: [
                            Center(child: Text('${day.day}', style: dayStyle)),
                            datesWithExpenseIcon
                                    .any((date) => isSameDay(date, day))
                                ? Positioned(
                                    right: 0,
                                    top: 12,
                                    // ignore: prefer_const_constructors
                                    child: Container(
                                      //color: Colors.blueAccent,
                                      height: 8,
                                      width: 8,
                                      decoration: BoxDecoration(
                                          color: Colors
                                              .redAccent, // Sfondo rosso intenso
                                          borderRadius:
                                              BorderRadius.circular(10.0)),
                                    ),
                                  )
                                : const SizedBox(),
                            datesWithIncomeIcon
                                    .any((date) => isSameDay(date, day))
                                ? Positioned(
                                    right: 5,
                                    top: 12,
                                    // ignore: prefer_const_constructors
                                    child: Container(
                                      //color: Colors.blueAccent,
                                      height: 8,
                                      width: 8,
                                      decoration: BoxDecoration(
                                          color: Colors
                                              .blueAccent, // Sfondo rosso intenso
                                          borderRadius:
                                              BorderRadius.circular(10.0)),
                                    ),
                                  )
                                : const SizedBox(),
                          ],
                        );
                      },
                    ),
                  ))),
          const SizedBox(
            height: 2,
          ),
          //SizedBox(height: 240),
          Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                Expanded(
                  child: Container(
                    //width: 200.0, // Imposta una larghezza per il Container
                    height: 200.0, // Imposta un'altezza per il Container
                    margin: const EdgeInsets.symmetric(horizontal: 10.0),
                    padding: const EdgeInsets.all(
                        10), // Aggiungi del padding se necessario
                    decoration: BoxDecoration(
                      color: Colors
                          .white, // Scegli un colore per lo sfondo del Container
                      borderRadius: BorderRadius.circular(
                          20), // Arrotonda i bordi se lo desideri
                      // Altre decorazioni per il Container...
                    ),
                    child: CustomPaint(
                      painter: CustomCircularProgress(
                          value: totalFixedIncomes -
                                      totalFixedExpenses -
                                      totalExpensesOfMonth >
                                  0
                              ? controller.value *
                                  (totalFixedIncomes -
                                      totalFixedExpenses -
                                      totalExpensesOfMonth) /
                                  totalFixedIncomes
                              : 0,
                          residuo:
                              "€ ${(totalFixedIncomes - totalFixedExpenses - totalExpensesOfMonth).toStringAsFixed(2)}",
                          entrateFisse: totalFixedIncomes.toStringAsFixed(2),
                          usciteFisse: totalFixedExpenses.toStringAsFixed(2),
                          usciteMese: totalExpensesOfMonth.toStringAsFixed(2)),
                    ),
                  ),
                ),
              ]),
          const SizedBox(
            height: 5,
          ),
          Padding(
              padding: const EdgeInsets.all(10.0),
              child: Container(
                height: 300.0,
                decoration: const BoxDecoration(
                  borderRadius: BorderRadius.only(
                    topLeft: Radius.circular(
                        20.0), // Angolo arrotondato in alto a sinistra
                    topRight: Radius.circular(
                        20.0), // Angolo arrotondato in alto a destra
                  ),
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [
                      Color.fromARGB(255, 0, 39, 106), // Blu intenso
                      Color.fromARGB(255, 96, 62, 189), // Fucsia
                      // Arancione fluo
                    ],
                  ),
                ),
                child: Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: <Widget>[
                      const Row(
                        mainAxisAlignment: MainAxisAlignment.start,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            '   Previsioni Risparmio',
                            style: TextStyle(
                              color: Colors.white,
                              fontSize: 18,
                              fontWeight: FontWeight.normal,
                            ),
                          ),
                        ],
                      ),
                      const Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        mainAxisAlignment: MainAxisAlignment.start,
                        children: <Widget>[
                          SizedBox(width: 15), // Spazio tra icona e testo
                          Expanded(
                            child: Text(
                              'Le previsioni di risparmio e spese sono calcolate da un algoritmo sulla base delle tue abitudini di consumo rilevate durante il mese in corso.',
                              style:
                                  TextStyle(color: Colors.white, fontSize: 8),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(
                        height: 10,
                      ),
                      Text(
                        '€ ${(totalFixedIncomes - totalFixedExpenses - forecastExpensesOfMonth).toStringAsFixed(2)}',
                        style: TextStyle(
                          color: totalFixedIncomes -
                                      totalFixedExpenses -
                                      forecastExpensesOfMonth <
                                  0
                              ? Colors.redAccent
                              : Colors.white,
                          fontSize: 30,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 15),
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.center,
                        mainAxisAlignment: MainAxisAlignment.spaceAround,
                        children: <Widget>[
                          Expanded(
                            child: buildColumnItem(
                                Icons.money,
                                "Previsione spesa a fine mese*",
                                '€ ${forecastExpensesOfMonth.toStringAsFixed(2)}'),
                          ),
                          Expanded(
                            child: buildColumnItem(
                                Icons.graphic_eq,
                                "Quanto spendi in media al giorno?",
                                '€ ${(totalExpensesOfMonth / DateTime.now().day).toStringAsFixed(2)}'),
                          ),
                          Expanded(
                            child: buildColumnItem(
                                Icons.lightbulb,
                                "Quanto puoi spendere al giorno?",
                                '€ ${dailyBudget.toStringAsFixed(2)}'),
                          ),
                        ],
                      ),
                      const SizedBox(
                        height: 10,
                      ),
                    ],
                  ),
                ),
              )),
          //SizedBox(height: 240),

          const SizedBox(
            height: 10,
          )
        ])));
  }

  void calculateForecastOfMonth() {
    final now = DateTime.now();
    final startOfMonth = DateTime(now.year, now.month, 1);
    final endOfMonth = DateTime(now.year, now.month + 1, 0);

    totalExpensesOfMonth = expenseBox.values
        .where((expense) =>
            expense.date.isAfter(startOfMonth) &&
            expense.date.isBefore(endOfMonth))
        .fold(0.0, (sum, item) => sum + item.amount);
  }

  Future<void> showAddFixedEntryDialog(BuildContext context,
      {bool isExpense = true}) async {
    String? selectedCategory;
    final TextEditingController amountController = TextEditingController();

    await showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text(
              isExpense ? 'Aggiungi Spesa Fissa' : 'Aggiungi Entrata Fissa'),
          content: SingleChildScrollView(
            child: ListBody(
              children: <Widget>[
                DropdownButton<String>(
                  value: selectedCategory,
                  hint: const Text('Seleziona Categoria'),
                  items: (isExpense ? constUsciteFisse : constEntrateFisse)
                      .map<DropdownMenuItem<String>>((String value) {
                    return DropdownMenuItem<String>(
                      value: value,
                      child: Text(value),
                    );
                  }).toList(),
                  onChanged: (String? newValue) {
                    setState(() {
                      selectedCategory = newValue;
                    });
                  },
                ),
                TextField(
                  controller: amountController,
                  decoration: const InputDecoration(
                    labelText: 'Importo',
                  ),
                  keyboardType: TextInputType.number,
                ),
                // Altre widget...
              ],
            ),
          ),
          actions: <Widget>[
            ElevatedButton(
              child: const Text('Salva'),
              onPressed: () {
                if (selectedCategory != null &&
                    double.tryParse(amountController.text) != null) {
                  (isExpense
                      ? saveFixedExpense(selectedCategory!,
                          double.parse(amountController.text))
                      : saveFixedIncome(selectedCategory!,
                          double.parse(amountController.text)));
                  Navigator.of(context).pop();
                }
              },
            ),
          ],
        );
      },
    );
  }

  void saveFixedIncome(String category, double amount) {
    final income = FixedIncome()
      ..category = category
      ..amount = amount;

    HiveManager().addFixedIncome(income);
  }

  void saveFixedExpense(String category, double amount) {
    final expense = FixedExpense()
      ..category = category
      ..amount = amount;

    HiveManager().addFixedExpense(expense);
  }

  Widget buildColumnItem(IconData icon, String smallText, String largeText) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.center,
      mainAxisAlignment: MainAxisAlignment.center,
      children: <Widget>[
        Container(
          width: 40,
          height: 40,
          decoration: const BoxDecoration(
            color: Colors.white,
            shape: BoxShape.circle,
          ),
          child: Icon(
            icon,
            color: (smallText.contains("registrate")
                ? Colors.redAccent
                : smallText.contains("previste")
                    ? Colors.indigoAccent
                    : smallText.contains("giorno")
                        ? Colors.deepOrange
                        : Colors.black),
          ),
        ),
        const SizedBox(height: 8),
        Text(
          smallText,
          style: const TextStyle(color: Colors.white, fontSize: 9),
          textAlign: TextAlign.center,
        ),
        Text(
          largeText,
          style: const TextStyle(
              color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
          textAlign: TextAlign.center,
        ),
      ],
    );
  }
} // END CLASS

// *************************************************
//
// GESTIONE DATABASE HIVE
//
// *************************************************

class HiveManager {
  static final HiveManager _instance = HiveManager._internal();
  factory HiveManager() {
    return _instance;
  }

  HiveManager._internal();

  // ogni box corrisponde ad una tabella DB più o meno

  Box<Expense>? expenseBox;
  Box<Income>? incomeBox;
  Box<FixedIncome>? fixedIncomeBox;
  Box<FixedExpense>? fixedExpenseBox;

  Future<void> initializeHive() async {
    final appDocumentDir = await getApplicationDocumentsDirectory();
    Hive.init(appDocumentDir.path);
    Hive.registerAdapter(ExpenseAdapter());
    Hive.registerAdapter(IncomeAdapter());
    Hive.registerAdapter(FixedIncomeAdapter());
    Hive.registerAdapter(FixedExpenseAdapter());

    incomeBox = await Hive.openBox<Income>('incomes');
    expenseBox = await Hive.openBox<Expense>('expenses');
    fixedIncomeBox = await Hive.openBox<FixedIncome>('fixedIncomes');
    fixedExpenseBox = await Hive.openBox<FixedExpense>('fixedExpenses');
  }

  double calculateTotalExpensesOfMonth(DateTime month) {
    double total = 0.0;
    final startOfMonth = DateTime(month.year, month.month, 1);
    final endOfMonth = DateTime(month.year, month.month + 1, 0);

    if (expenseBox != null) {
      final expenses = expenseBox!.values.where((expense) =>
          expense.date
              .isAfter(startOfMonth.subtract(const Duration(days: 1))) &&
          expense.date.isBefore(endOfMonth.add(const Duration(days: 1))));

      for (var expense in expenses) {
        total += expense.amount;
      }
    }
    return total;
  }

  double calculateForecastOfMonth(DateTime month) {
    final now = DateTime.now();
    final startOfMonth = DateTime(now.year, now.month, 1);
    final endOfMonth = DateTime(now.year, now.month + 1, 0);
    final today = DateTime(now.year, now.month, now.day);

    double totalExpensesUntilToday = 0.0;
    totalExpensesUntilToday = expenseBox!.values
        .where((expense) =>
            expense.date
                .isAfter(startOfMonth.subtract(const Duration(days: 1))) &&
            expense.date.isBefore(today.add(const Duration(days: 1))))
        .fold(0.0, (sum, item) => sum + item.amount);

    int daysPassed = now.day;
    int totalDaysOfMonth = endOfMonth.day;

    double forecast = totalExpensesUntilToday / daysPassed * totalDaysOfMonth;

    return forecast;
  }

  Future<List<DateTime>> getDatesWithExpenses(meseSelezionato) async {
    final firstDayOfMonth =
        DateTime(meseSelezionato.year, meseSelezionato.month, 1);
    final lastDayOfMonth =
        DateTime(meseSelezionato.year, meseSelezionato.month + 1, 0);

    final expenses = expenseBox?.values ?? [];

    var dates = <DateTime>{}; // nuovo

    for (var expense in expenses) {
      if (expense.date
              .isAfter(firstDayOfMonth.subtract(const Duration(days: 1))) &&
          expense.date.isBefore(lastDayOfMonth.add(const Duration(days: 1)))) {
        dates.add(
            DateTime(expense.date.year, expense.date.month, expense.date.day));
      }
    }

    return dates.toList();
  }

  Future<List<DateTime>> getDatesWithIncomes(meseSelezionato) async {
    final firstDayOfMonth =
        DateTime(meseSelezionato.year, meseSelezionato.month, 1);
    final lastDayOfMonth =
        DateTime(meseSelezionato.year, meseSelezionato.month + 1, 0);

    final incomes = incomeBox?.values ?? [];

    var dates = <DateTime>{}; // nuovo

    for (var income in incomes) {
      if (income.date
              .isAfter(firstDayOfMonth.subtract(const Duration(days: 1))) &&
          income.date.isBefore(lastDayOfMonth.add(const Duration(days: 1)))) {
        dates.add(
            DateTime(income.date.year, income.date.month, income.date.day));
      }
    }
    return dates.toList();
  }

  void addFixedIncome(FixedIncome income) {
    fixedIncomeBox?.add(income);
  }

  void addFixedExpense(FixedExpense expense) {
    fixedExpenseBox?.add(expense);
  }

  List<FixedIncome> getFixedIncomes() {
    return fixedIncomeBox?.values.toList() ?? [];
  }

  List<FixedExpense> getFixedExpenses() {
    return fixedExpenseBox?.values.toList() ?? [];
  }

  // Altri metodi utili...
}

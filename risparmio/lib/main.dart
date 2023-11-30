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
  DateTime _selectedDay = DateTime.now();
  DateTime _focusedDay = DateTime.now();

  List<DateTime> datesWithIcon = [];

  List<FixedIncome> fixedIncomes = [];
  List<FixedExpense> fixedExpenses = [];
  double totalFixedIncomes = 0.0;
  double totalFixedExpenses = 0.0;
  double dailyBudget = 0.0;

  late AnimationController controller;

  @override
  void initState() {
    super.initState();
    loadDatesWithExpenses();
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

  void loadDatesWithExpenses() async {
    datesWithIcon = await HiveManager().getDatesWithExpenses();
    setState(() {}); // Aggiorna lo stato per riflettere i nuovi dati
  }

  void calculateFinancialStats() async {
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
  // Per calcolare le spese totali
  double totalExpensesOfMonth =
      HiveManager().calculateTotalExpensesOfMonth(DateTime.now());

  // Per Calcolare il FOrecast di spesa
  double forecastExpensesOfMonth =
      HiveManager().calculateForecastOfMonth(DateTime.now());

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
                            color: Colors.red,
                            fontWeight: FontWeight.bold)),
                    daysOfWeekHeight: 28,
                    calendarStyle: const CalendarStyle(
                      defaultTextStyle:
                          TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
                      weekendTextStyle: TextStyle(
                          fontSize: 42,
                          color: Colors.red,
                          fontWeight: FontWeight.bold),
                      todayDecoration: BoxDecoration(
                        color: Colors
                            .blueAccent, // Sostituisci con il tuo colore preferito
                        shape: BoxShape.rectangle,
                      ),
                    ),
                    startingDayOfWeek: StartingDayOfWeek.monday,
                    focusedDay: _focusedDay,
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
                    onPageChanged: (focusedDay) {
                      _focusedDay = focusedDay;
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
                              color: Colors.red,
                              fontWeight: FontWeight.bold);
                        } else {
                          dayStyle = const TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                              color: Colors.black);
                        }

                        if (datesWithIcon.any((date) => isSameDay(date, day))) {
                          // RestColor.fromARGB(137, 1, 0, 0)get che combina il numero del giorno e l'icona
                          return Stack(
                            children: [
                              Center(
                                  child: Text('${day.day}', style: dayStyle)),
                              // ignore: prefer_const_constructors
                              Positioned(
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
                              ),
                            ],
                          );
                        } else {
                          // Per gli altri giorni, mostra solo il numero del giorno
                          return Center(
                              child: Text('${day.day}', style: dayStyle));
                        }
                      },
                    ),
                  ))),
          SizedBox(
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
                    padding: EdgeInsets.all(
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
          SizedBox(
            height: 5,
          ),
          Padding(
              padding: const EdgeInsets.all(10.0),
              child: Container(
                height: 300.0,
                decoration: BoxDecoration(
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
                      Row(
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
                      Row(
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
                      SizedBox(
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
                      SizedBox(height: 15),
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.center,
                        mainAxisAlignment: MainAxisAlignment.spaceAround,
                        children: <Widget>[
                          Expanded(
                            child: buildColumnItem(
                                Icons.money,
                                "Totale spese registrate nel mese",
                                '€ ${totalExpensesOfMonth.toStringAsFixed(2)}'),
                          ),
                          Expanded(
                            child: buildColumnItem(
                                Icons.calendar_month,
                                "Totale spese previste a fine mese *",
                                '€ ${forecastExpensesOfMonth.toStringAsFixed(2)}'),
                          ),
                          Expanded(
                            child: buildColumnItem(
                                Icons.lightbulb,
                                "Quanto puoi spendere ogni giorno?",
                                '€ ${dailyBudget.toStringAsFixed(2)}'),
                          ),
                        ],
                      ),
                      SizedBox(
                        height: 10,
                      ),
                    ],
                  ),
                ),
              )),
          //SizedBox(height: 240),

          SizedBox(
            height: 10,
          )
        ])));
  }

  void calculateTotalExpensesOfMonth() {
    final now = DateTime.now();
    final startOfMonth = DateTime(now.year, now.month, 1);
    final endOfMonth = DateTime(now.year, now.month + 1, 0);

    totalExpensesOfMonth = expenseBox.values
        .where((expense) =>
            expense.date.isAfter(startOfMonth) &&
            expense.date.isBefore(endOfMonth))
        .fold(0.0, (sum, item) => sum + item.amount);
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
                  hint: Text('Seleziona Categoria'),
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
          decoration: BoxDecoration(
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
        SizedBox(height: 8),
        Text(
          smallText,
          style: TextStyle(color: Colors.white, fontSize: 9),
          textAlign: TextAlign.center,
        ),
        Text(
          largeText,
          style: TextStyle(
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
  Box<FixedIncome>? fixedIncomeBox;
  Box<FixedExpense>? fixedExpenseBox;

  Future<void> initializeHive() async {
    final appDocumentDir = await getApplicationDocumentsDirectory();
    Hive.init(appDocumentDir.path);
    Hive.registerAdapter(ExpenseAdapter());
    Hive.registerAdapter(FixedIncomeAdapter());
    Hive.registerAdapter(FixedExpenseAdapter());

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

  Future<List<DateTime>> getDatesWithExpenses() async {
    var dates = <DateTime>{};
    final now = DateTime.now();
    final firstDayOfMonth = DateTime(now.year, now.month, 1);
    final lastDayOfMonth = DateTime(now.year, now.month + 1, 0);

    final expenses = expenseBox?.values ?? [];
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

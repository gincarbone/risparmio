import 'package:flutter/material.dart';
import 'package:risparmio/main.dart';
import 'package:risparmio/models/hive.dart'; // Sostituisci con il percorso corretto
import 'package:risparmio/models/constants.dart';
import 'dart:developer' as dev;

class MainDrawer extends StatefulWidget {
  @override
  _MainDrawerState createState() => _MainDrawerState();
}

class _MainDrawerState extends State<MainDrawer> {
  List<FixedIncome> fixedIncomes = [];
  List<FixedExpense> fixedExpenses = [];

  double totalFixedIncomes = 0.0;
  double totalFixedExpenses = 0.0;
  double dailyBudget = 0.0;

  @override
  void initState() {
    super.initState();
    dev.log("OK INIZIAMO!");
    loadFixedData();
    calculateFinancialStats();
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

  void loadFixedData() async {
    fixedIncomes = HiveManager().getFixedIncomes();
    fixedExpenses = HiveManager().getFixedExpenses();
    dev.log("start");
    dev.log(fixedIncomes.toString());
    dev.log(fixedExpenses.toString());
    dev.log("end");
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return Drawer(
      //shadowColor: Colors.grey,
      child: ListView(
        children: <Widget>[
          const DrawerHeader(
            decoration: BoxDecoration(
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
            child: Column(
              children: <Widget>[
                Padding(
                  padding: EdgeInsets.all(8.0),
                  child: Icon(
                    Icons
                        .savings_outlined, // Sostituisci con l'icona che preferisci
                    size: 50.0,
                    color: Colors.white,
                  ),
                ),
                Text(
                  'Risparmio 1.0',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 9,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(
            height: 10,
          ),
          // DrawerHeader e altre voci...
          buildExpansionTile('Entrate Fisse', fixedIncomes, false),
          buildExpansionTile('Uscite Fisse', fixedExpenses, true),
          const SizedBox(
            height: 20,
          ),
          // DrawerHeader e altre voci...
          ListTile(
            title: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Text(
                    'Disponibilità: €${totalFixedIncomes - totalFixedExpenses}'),
                Text('Budget Giornaliero: €${dailyBudget.toStringAsFixed(2)}'),
              ],
            ),
          ),
        ],
      ),
    );
  }

  ExpansionTile buildExpansionTile(
      String title, List<dynamic> items, bool isExpense) {
    return ExpansionTile(
      title: Text(title),
      leading: Icon(isExpense ? Icons.arrow_circle_down : Icons.arrow_circle_up,
          color: isExpense ? Colors.redAccent : Colors.blueAccent),
      children: <Widget>[
        ListTile(
          title: const Text('Aggiungi'),
          leading: const Icon(Icons.add),
          onTap: () {
            // Apri il dialog per aggiungere una nuova entrata/uscita fissa
            showAddFixedEntryDialog(context, isExpense: isExpense);
          },
        ),
        ...items
            .map((item) => ListTile(
                  title: Text(
                    '${item.category}: € ${item.amount.toStringAsFixed(2)}',
                    style: const TextStyle(color: Colors.white, fontSize: 12),
                  ),
                  textColor: Colors.white,
                  leading: IconButton(
                    icon: const Icon(
                      Icons.delete_outline_outlined,
                      color: Colors.white,
                    ),
                    onPressed: () {
                      // Logica per eliminare l'elemento
                      showConfirmationDialog(context, item, isExpense);
                      //deleteItem(item, isExpense);
                    },
                  ),
                  tileColor: isExpense ? Colors.redAccent : Colors.blueAccent,
                  onTap: () {
                    // Apri il dialog di modifica per l'elemento esistente
                    showAddFixedEntryDialog(context,
                        isExpense: isExpense, existingItem: item);
                  },
                ))
            .toList(),
      ],
    );
  }

  Future<void> showAddFixedEntryDialog(BuildContext context,
      {required bool isExpense, dynamic existingItem}) async {
    // Pre-riempi i campi se `existingItem` non è null
    String? selectedCategory = existingItem?.category;
    TextEditingController amountController =
        TextEditingController(text: existingItem?.amount.toString());

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
                  loadFixedData();
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

  void deleteItem(dynamic item, bool isExpense) {
    // Logica per eliminare l'elemento da Hive e aggiornare la lista
    if (isExpense) {
      HiveManager().fixedExpenseBox?.delete(item.key);
    } else {
      HiveManager().fixedIncomeBox?.delete(item.key);
    }
    loadFixedData();
    calculateFinancialStats();
  }

  void showConfirmationDialog(
      BuildContext context, dynamic item, bool isExpense) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(20.0),
          ),
          title: const Text('Conferma Cancellazione'),
          content:
              const Text('Sei sicuro di voler cancellare questo elemento?'),
          actionsPadding:
              EdgeInsets.zero, // Rimuove il padding intorno alle azioni
          actions: <Widget>[
            Row(
                crossAxisAlignment: CrossAxisAlignment.center,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Expanded(
                      child: Container(
                    decoration: const BoxDecoration(
                      color: Colors.red,
                      borderRadius: BorderRadius.only(
                        bottomLeft: Radius.circular(20),
                      ),
                    ),
                    child: TextButton(
                      child: const Text('ANNULLA',
                          style: TextStyle(color: Colors.white)),
                      onPressed: () {
                        Navigator.of(context).pop(); // Chiude il dialogo
                      },
                    ),
                  )),
                  Expanded(
                    child: Container(
                      decoration: const BoxDecoration(
                        color: Colors.blue,
                        borderRadius: BorderRadius.only(
                          bottomRight: Radius.circular(20),
                        ),
                      ),
                      child: TextButton(
                        child: const Text('CONFERMA',
                            style: TextStyle(color: Colors.white)),
                        onPressed: () {
                          deleteItem(item, isExpense);
                          Navigator.of(context).pop(); // Chiude il dialogo
                        },
                      ),
                    ),
                  ),
                ]),
          ],
        );
      },
    );
  }
} //end class

import 'package:flutter/material.dart';
import 'package:hive/hive.dart';
import 'package:risparmio/main.dart';
import 'package:risparmio/models/hive.dart'; // Sostituisci con il percorso corretto
import 'package:intl/intl.dart';
import 'package:risparmio/models/constants.dart';

class ExpenseDetailsScreen extends StatefulWidget {
  final DateTime selectedDay;

  ExpenseDetailsScreen({Key? key, required this.selectedDay}) : super(key: key);

  @override
  _ExpenseDetailsScreenState createState() => _ExpenseDetailsScreenState();
}

class _ExpenseDetailsScreenState extends State<ExpenseDetailsScreen> {
  late Box<Expense> expenseBox;
  List<Expense> expenses = [];

  @override
  void initState() {
    super.initState();
    openHiveBox();
  }

  @override
  void dispose() {
    //expenseBox.close();
    super.dispose();
  }

  void loadExpenses() {
    if (expenseBox.isOpen) {
      setState(() {
        expenses = expenseBox.values
            .where((expense) => isSameDay(expense.date, widget.selectedDay))
            .toList();
      });
    }
  }

  void addExpense(double amount, String description) {
    final newExpense = Expense()
      ..amount = amount
      ..description = description
      ..date = widget.selectedDay;

    expenseBox.add(newExpense);
    loadExpenses();
  }

  Future openHiveBox() async {
    expenseBox = await Hive.openBox<Expense>('expenses');
    loadExpenses();
  }

  @override
  Widget build(BuildContext context) {
    String formattedDate =
        DateFormat('d MMMM', 'it_IT').format(widget.selectedDay);

    return Scaffold(
      backgroundColor: bgColor,
      appBar: AppBar(
        title: Text('Spese del $formattedDate'),
        leading: IconButton(
          icon: Icon(Icons.arrow_back),
          onPressed: () {
            // Qui inserisci il tuo comportamento personalizzato
            print('Azione personalizzata per il pulsante Indietro');
            loadExpenses();
            Navigator.push(
              context,
              MaterialPageRoute(builder: (context) => CalendarScreen()),
            );
          },
        ),
        // .
      ),
      body: Column(
        children: [
          Expanded(
            child: ListView(
              children: ListTile.divideTiles(
                context: context,
                tiles: List.generate(expenses.length, (index) {
                  final expense = expenses[index];
                  return SafeArea(
                    child: ListTile(
                      tileColor: Colors.white,
                      leading: IconButton(
                        icon: Icon(Icons.delete_outline_outlined,
                            color: Colors.grey),
                        onPressed: () {
                          // Logica per eliminare la spesa
                          showConfirmationDialog(context, expense);
                        },
                      ),
                      title: Text(
                        expense.category,
                        style: TextStyle(
                            fontSize: 14,
                            fontWeight: FontWeight.w600,
                            color: Colors.black87),
                      ),
                      trailing: Text(
                        '€ -${expense.amount.toStringAsFixed(2)}',
                        style: TextStyle(fontSize: 18, color: speseCol),
                      ),
                      subtitle: Text(
                        expense.description,
                        style: TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.normal,
                            color: Colors.black),
                      ),
                      onTap: () => showExpenseDialog(existingExpense: expense),
                    ),
                  );
                }),
              ).toList(),
            ),
          ),
          ElevatedButton(
            onPressed: () {
              // Logica per aggiungere una nuova spesa
              showExpenseDialog();
            },
            child: Icon(
              Icons.add,
              color: entrateCol,
              size: 40,
            ),
          ),
          SizedBox(height: 20),
        ],
      ),
    );
  }

  void showExpenseDialog({Expense? existingExpense}) async {
    //String? selectedCategory;
    //TextEditingController amountController = TextEditingController();

    String? selectedCategory = existingExpense?.category;
    //String? selectedDescription = existingExpense?.description;

    // Controller per le note
    TextEditingController noteController =
        TextEditingController(text: existingExpense?.description.toString());
    TextEditingController amountController =
        TextEditingController(text: existingExpense?.amount.toString());

    await showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Aggiungi Spesa'),
          content: StatefulBuilder(
            // Usa StatefulBuilder qui
            builder: (BuildContext context, StateSetter setState) {
              return SingleChildScrollView(
                  child: ListBody(
                children: <Widget>[
                  DropdownButton<String>(
                    value: selectedCategory,
                    hint: Text('Seleziona Categoria'),
                    items: constTipoSpese
                        .map<DropdownMenuItem<String>>((String value) {
                      return DropdownMenuItem<String>(
                        value: value,
                        child: Text(value),
                      );
                    }).toList(),
                    onChanged: (String? newValue) {
                      selectedCategory = newValue;
                      setState(() {});
                    },
                  ),
                  if (selectedCategory !=
                      null) // Mostra la categoria selezionata
                    Padding(
                      padding: const EdgeInsets.only(top: 8.0),
                      child: Text("Categoria: $selectedCategory"),
                    ),
                  TextField(
                    controller: amountController,
                    onChanged: (value) {
                      amountController.text = value.replaceAll(',', '.');
                    },
                    decoration: const InputDecoration(
                      labelText: 'Importo',
                    ),
                    textAlign: TextAlign.center,
                    keyboardType: TextInputType.number,
                    style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                        color: speseCol),
                  ),
                  TextField(
                    controller: noteController,
                    decoration: const InputDecoration(
                      labelText: 'Note',
                    ),
                    maxLength: 24, // Massimo 8 parole
                    maxLines: 2, // Massimo 2 righe
                  ),
                ],
              ));
            },
          ),
          actions: <Widget>[
            ElevatedButton(
              style: ElevatedButton.styleFrom(
                primary: Colors.blueAccent,
              ),
              onPressed: () {
                double? amount = double.tryParse(amountController.text);
                String notes = noteController.text;
                if (selectedCategory != null && amount != null) {
                  if (existingExpense != null) {
                    // Aggiorna l'esistente
                    updateExpense(
                        existingExpense, amount, selectedCategory!, notes);
                  } else {
                    // Crea una nuova
                    saveExpense(amount, selectedCategory!, notes);
                  }
                  Navigator.of(context).pop(); // Chiudi il dialogo
                }
              },
              child: const Text('Conferma',
                  style: TextStyle(
                      color: Colors.white, fontWeight: FontWeight.bold)),
            ),
          ],
        );
      },
    );
  }

  void deleteExpense(Expense expense) async {
    // Elimina l'elemento dal database
    expenseBox.delete(expense.key);

    // Aggiorna la lista delle spese per riflettere la modifica
    loadExpenses();

    // Eventualmente, mostra un messaggio di conferma
  }

  void saveExpense(double amount, String category, String description) {
    final newExpense = Expense()
      ..amount = amount
      ..description = description
      ..category = category
      ..date = widget.selectedDay;

    expenseBox.add(newExpense);
    loadExpenses();
  }

  void updateExpense(
      Expense expense, double amount, String category, String description) {
    expense
      ..amount = amount
      ..category = category
      ..description = description;
    expense.save(); // Assicurati che il modello Expense estenda HiveObject
    loadExpenses();
  }

  void showConfirmationDialog(BuildContext context, dynamic item) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(20.0),
          ),
          title: Text('Conferma Cancellazione'),
          content: Text('Sei sicuro di voler cancellare questo elemento?'),
          actionsPadding:
              EdgeInsets.zero, // Rimuove il padding intorno alle azioni
          actions: <Widget>[
            Row(
                crossAxisAlignment: CrossAxisAlignment.center,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Expanded(
                      child: Container(
                    decoration: BoxDecoration(
                      color: Colors.red,
                      borderRadius: BorderRadius.only(
                        bottomLeft: Radius.circular(20),
                      ),
                    ),
                    child: TextButton(
                      child: Text('ANNULLA',
                          style: TextStyle(color: Colors.white)),
                      onPressed: () {
                        Navigator.of(context).pop(); // Chiude il dialogo
                      },
                    ),
                  )),
                  Expanded(
                    child: Container(
                      decoration: BoxDecoration(
                        color: Colors.blue,
                        borderRadius: BorderRadius.only(
                          bottomRight: Radius.circular(20),
                        ),
                      ),
                      child: TextButton(
                        child: Text('CONFERMA',
                            style: TextStyle(color: Colors.white)),
                        onPressed: () {
                          deleteExpense(item);
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
} // end class

bool isSameDay(DateTime date1, DateTime date2) {
  return date1.year == date2.year &&
      date1.month == date2.month &&
      date1.day == date2.day;
}

// ignore_for_file: library_private_types_in_public_api

import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:hive/hive.dart';
import 'package:risparmio/models/hive.dart'; // Assicurati che il percorso sia corretto
import 'dart:math';

class Stats extends StatefulWidget {
  @override
  _StatsState createState() => _StatsState();
}

class _StatsState extends State<Stats> {
  late Map<String, double> categoryTotals;
  double totalSpent = 0;
  late List<PieChartSectionData> sections;

  @override
  void initState() {
    super.initState();
    loadData();
  }

  void loadData() async {
    var box = Hive.box<Expense>('expenses');
    Map<String, double> tempCategoryTotals = {};
    double tempTotal = 0;

    for (var expense in box.values) {
      double amount = expense.amount;
      String category = expense.category;

      tempTotal += amount;

      // Assicurati che la categoria esista nel dizionario con un valore inizializzato
      if (!tempCategoryTotals.containsKey(category)) {
        tempCategoryTotals[category] = 0;
      }
      // Aggiorna il totale per la categoria
      tempCategoryTotals[category] = tempCategoryTotals[category]! + amount;
    }

    setState(() {
      categoryTotals = tempCategoryTotals;
      totalSpent = tempTotal;
    });
  }

  Widget buildLegend() {
    return Wrap(
      spacing: 8.0, // Spazio orizzontale tra le colonne
      runSpacing: 4.0, // Spazio verticale tra le righe
      children: sections.map((section) {
        return Row(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            Icon(Icons.circle, color: section.color, size: 12),
            const SizedBox(width: 8),
            Text(section.title, style: const TextStyle(fontSize: 14)),
          ],
        );
      }).toList(),
    );
  }

  @override
  Widget build(BuildContext context) {
    List<PieChartSectionData> sections = categoryTotals.entries
        .map((entry) => PieChartSectionData(
              value: entry.value,
              title: '${entry.key} (${entry.value.toStringAsFixed(2)})',
              color:
                  Colors.primaries[Random().nextInt(Colors.primaries.length)],
            ))
        .toList();

    return Scaffold(
      appBar: AppBar(title: const Text('Statistiche Spese')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Expanded(
              child: PieChart(
                PieChartData(
                  sections: sections,
                  centerSpaceRadius: 90,
                  sectionsSpace: 5,
                  startDegreeOffset: 270,
                  borderData: FlBorderData(
                    show: true,
                  ),
                ),
              ),
            ),
            Text(
              'Spesa Totale: €$totalSpent',
              style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
          ],
        ),
      ),
    );
  }
}

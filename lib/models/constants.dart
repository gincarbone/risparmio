import 'package:flutter/material.dart';

const appVersion = '1.1.0+3';

Color bgColor = const Color(0xFFF4F4F4);

Color speseCol = const Color.fromARGB(255, 239, 149, 149);
Color entrateCol = const Color.fromARGB(255, 149, 161, 239);

const List<String> constTipoSpese = [
  'Utenze',
  'Carburante',
  'Spesa',
  'Bambini',
  'Acquisti Online',
  'Abbigliamento',
  'Imprevisti',
  'Meccanico',
  'Multe',
  'Pranzi e Cene',
  'Abbonamenti',
  'Vacanze',
  'Altro'
]; // gestire categorie su db hive

const List<String> constTipoEntrate = [
  'Vendite Prodotti',
  'Servizi',
  'Lavoretti',
  'Vincite',
  'Riscossioni',
  'Altro'
]; // gestire categorie su db hive

const List<String> constEntrateFisse = [
  'Stipendi',
  'Affitti',
  'Altro'
]; // gestire categorie su db hive

const List<String> constUsciteFisse = [
  'Mutui',
  'Finanziamento',
  'Assegni Alimenti',
  'Rate Diverse',
  'Altro'
];// gestire categorie su db hive

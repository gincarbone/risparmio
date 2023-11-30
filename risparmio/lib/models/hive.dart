import 'package:hive/hive.dart';

part 'hive.g.dart';

@HiveType(typeId: 0)
class Expense extends HiveObject {
  @HiveField(0)
  late double amount;

  @HiveField(1)
  late String description;

  @HiveField(2)
  late DateTime date; // Data della spesa
}

@HiveType(typeId: 1)
class FixedIncome extends HiveObject {
  @HiveField(0)
  late String category;

  @HiveField(1)
  late double amount;
}

@HiveType(typeId: 2)
class FixedExpense extends HiveObject {
  @HiveField(0)
  late String category;

  @HiveField(1)
  late double amount;
}

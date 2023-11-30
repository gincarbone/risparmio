// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hive.dart';

// **************************************************************************
// TypeAdapterGenerator
// **************************************************************************

class ExpenseAdapter extends TypeAdapter<Expense> {
  @override
  final int typeId = 0;

  @override
  Expense read(BinaryReader reader) {
    final numOfFields = reader.readByte();
    final fields = <int, dynamic>{
      for (int i = 0; i < numOfFields; i++) reader.readByte(): reader.read(),
    };
    return Expense()
      ..amount = fields[0] as double
      ..description = fields[1] as String
      ..date = fields[2] as DateTime;
  }

  @override
  void write(BinaryWriter writer, Expense obj) {
    writer
      ..writeByte(3)
      ..writeByte(0)
      ..write(obj.amount)
      ..writeByte(1)
      ..write(obj.description)
      ..writeByte(2)
      ..write(obj.date);
  }

  @override
  int get hashCode => typeId.hashCode;

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is ExpenseAdapter &&
          runtimeType == other.runtimeType &&
          typeId == other.typeId;
}

class FixedIncomeAdapter extends TypeAdapter<FixedIncome> {
  @override
  final int typeId = 1;

  @override
  FixedIncome read(BinaryReader reader) {
    final numOfFields = reader.readByte();
    final fields = <int, dynamic>{
      for (int i = 0; i < numOfFields; i++) reader.readByte(): reader.read(),
    };
    return FixedIncome()
      ..category = fields[0] as String
      ..amount = fields[1] as double;
  }

  @override
  void write(BinaryWriter writer, FixedIncome obj) {
    writer
      ..writeByte(2)
      ..writeByte(0)
      ..write(obj.category)
      ..writeByte(1)
      ..write(obj.amount);
  }

  @override
  int get hashCode => typeId.hashCode;

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is FixedIncomeAdapter &&
          runtimeType == other.runtimeType &&
          typeId == other.typeId;
}

class FixedExpenseAdapter extends TypeAdapter<FixedExpense> {
  @override
  final int typeId = 2;

  @override
  FixedExpense read(BinaryReader reader) {
    final numOfFields = reader.readByte();
    final fields = <int, dynamic>{
      for (int i = 0; i < numOfFields; i++) reader.readByte(): reader.read(),
    };
    return FixedExpense()
      ..category = fields[0] as String
      ..amount = fields[1] as double;
  }

  @override
  void write(BinaryWriter writer, FixedExpense obj) {
    writer
      ..writeByte(2)
      ..writeByte(0)
      ..write(obj.category)
      ..writeByte(1)
      ..write(obj.amount);
  }

  @override
  int get hashCode => typeId.hashCode;

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is FixedExpenseAdapter &&
          runtimeType == other.runtimeType &&
          typeId == other.typeId;
}

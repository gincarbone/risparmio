import 'package:flutter/material.dart';
import 'package:vector_math/vector_math.dart' as vmath;
import 'dart:math' as math;

class CustomCircularProgress extends CustomPainter {
  final double value;
  final String residuo;
  final String entrateFisse; // Aggiungi un campo per il testo
  final String usciteFisse;
  final String usciteMese;

  CustomCircularProgress(
      {required this.value,
      required this.residuo,
      required this.entrateFisse,
      required this.usciteFisse,
      required this.usciteMese});

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2 + 50, size.height / 2);

    // Aggiungi titolo
    final textTitolo = TextSpan(
      text: "Risparmio",
      style: TextStyle(
        color: Colors.black54,
        fontSize: 22,
        fontWeight: FontWeight.normal,
      ),
    );
    final textPainterTitolo = TextPainter(
      text: textTitolo,
      textDirection: TextDirection.ltr,
    );
    textPainterTitolo.layout(
      minWidth: 0,
      maxWidth: size.width,
    );
    final offsetTitolo = Offset(
      (size.width - textPainterTitolo.width) / 2 - 120,
      (size.height - textPainterTitolo.height) / 2 - 70,
    );
    textPainterTitolo.paint(canvas, offsetTitolo);

    // Aggiungi il testo al centro
    final textSpan = TextSpan(
      text: this.residuo,
      style: TextStyle(
        color: Colors.black,
        fontSize: 28,
        fontWeight: FontWeight.bold,
      ),
    );
    final textPainter = TextPainter(
      text: textSpan,
      textDirection: TextDirection.ltr,
    );
    textPainter.layout(
      minWidth: 0,
      maxWidth: size.width,
    );
    final offset = Offset(
      (size.width - textPainter.width) / 2 + 50,
      (size.height - textPainter.height) / 2,
    );
    textPainter.paint(canvas, offset);

    canvas.drawArc(
      Rect.fromCenter(center: center, width: 170, height: 170),
      vmath.radians(140),
      vmath.radians(260),
      false,
      Paint()
        ..style = PaintingStyle.stroke
        ..color = Colors.black12
        ..strokeCap = StrokeCap.round
        ..strokeWidth = 20,
    );
    canvas.saveLayer(
      Rect.fromCenter(center: center, width: 200, height: 200),
      Paint(),
    );

    const Gradient gradient = SweepGradient(
      startAngle: 1.25 * math.pi / 2,
      endAngle: 5.5 * math.pi / 2,
      tileMode: TileMode.clamp,
      colors: <Color>[
        Colors.deepPurple,
        Colors.blueAccent,
      ],
    );
    canvas.drawArc(
      Rect.fromCenter(center: center, width: 170, height: 170),
      vmath.radians(140),
      vmath.radians(260 * value),
      false,
      Paint()
        ..style = PaintingStyle.stroke
        ..strokeCap = StrokeCap.round
        ..shader = gradient
            .createShader(Rect.fromLTWH(0.0, 0.0, size.width, size.height))
        ..strokeWidth = 20,
    );
    canvas.restore();

    // Calcola la posizione per le icone e il testo sul lato destro
    final double iconSize = 10.0;
    final double textHeight = 16.0;
    final double descTextHeight = 12.0;
    final double spaceBetween = 10.0; // Spazio tra le righe

    // Prima riga
    _drawIconAndText(
        canvas,
        size,
        iconSize,
        textHeight,
        descTextHeight,
        " -$usciteFisse",
        "Uscite Fisse",
        Offset(size.width - 340, size.height - 80 - spaceBetween),
        Colors.redAccent);

    // Seconda riga
    _drawIconAndText(
        canvas,
        size,
        iconSize,
        textHeight,
        descTextHeight,
        entrateFisse,
        "Entrate fisse",
        Offset(
            size.width - 340, size.height / 2 + spaceBetween - 66 + textHeight),
        Colors.blueAccent);

    // Prima riga
    _drawIconAndText(
        canvas,
        size,
        iconSize,
        textHeight,
        descTextHeight,
        " -$usciteMese",
        "Uscite del Mese",
        Offset(size.width - 340, size.height - 40 - spaceBetween),
        Colors.redAccent);
  }

  void _drawIconAndText(
      Canvas canvas,
      Size size,
      double iconSize,
      double textHeight,
      double descTextHeight,
      String amount,
      String description,
      Offset offset,
      Color color) {
    // Icona
    Paint iconPaint = Paint()..color = color;
    // Utilizza qui un'icona appropriata. Per questo esempio, disegno un cerchio.
    canvas.drawCircle(offset, iconSize / 2, iconPaint);

    // Importo
    final amountTextSpan = TextSpan(
      text: amount,
      style: TextStyle(
        color: Colors.black,
        fontSize: textHeight,
        fontWeight: FontWeight.bold,
      ),
    );
    _drawText(canvas, amountTextSpan,
        Offset(offset.dx + iconSize + 5, offset.dy - textHeight / 2));

    // Descrizione
    final descTextSpan = TextSpan(
      text: description,
      style: TextStyle(
        color: Colors.black,
        fontSize: descTextHeight,
      ),
    );
    _drawText(canvas, descTextSpan,
        Offset(offset.dx + iconSize + 5, offset.dy + 5 + descTextHeight / 2));
  }

  void _drawText(Canvas canvas, TextSpan textSpan, Offset offset) {
    final textPainter = TextPainter(
      text: textSpan,
      textDirection: TextDirection.ltr,
    );
    textPainter.layout(minWidth: 0, maxWidth: double.infinity);
    textPainter.paint(canvas, offset);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
}

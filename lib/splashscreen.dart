import 'package:flutter/material.dart';
import 'dart:async';
import 'package:risparmio/main.dart'; // Assicurati che questo import sia corretto

class SplashScreen extends StatefulWidget {
  @override
  _SplashScreenState createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  @override
  void initState() {
    super.initState();
    Timer(const Duration(seconds: 4), () {
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(
          builder: (_) =>
              const CalendarScreen(), // Sostituisci con il tuo widget della schermata principale
        ),
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.blueAccent,
      body: Container(
        width: double.infinity,
        height: double.infinity,
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              Color.fromARGB(255, 0, 39, 106), // Blu intenso
              Color.fromARGB(255, 96, 62, 189), // Fucsia
            ],
          ),
        ),
        child: const Column(
          children: <Widget>[
            Spacer(), // Utilizza Spacer per centrare verticalmente l'icona e il testo
            Icon(
              Icons.savings_outlined, // Sostituisci con l'icona che preferisci
              size: 100.0,
              color: Colors.white,
            ),
            Text(
              "Risparmio",
              style: TextStyle(
                color: Colors.white,
                fontSize: 12.0,
                fontWeight: FontWeight.normal,
              ),
            ),
            Spacer(), // Continua a utilizzare Spacer per mantenere centrato il contenuto
            Align(
              alignment: Alignment.bottomCenter,
              child: Padding(
                padding: EdgeInsets.all(20.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: <Widget>[
                    Text("You&Media",
                        style: TextStyle(fontSize: 10, color: Colors.white)),
                    Text("v. 1.0.1",
                        style: TextStyle(fontSize: 10, color: Colors.white)),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:integration_test_sample/main.dart';

void main() {
  testWidgets('Main screen shows list and navigates to new screen',
      (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const MyApp(title: 'Test'));

    // Verify required widgets are present on the main screen.
    final Finder openNewScreenButton = find.byKey(const ValueKey('NewScreen'));
    expect(openNewScreenButton, findsOneWidget);
    expect(find.text('Open new screen'), findsOneWidget);
    expect(find.widgetWithText(TextField, 'Text Input'), findsOneWidget);

    // Verify tapping the button navigates to screen 1.
    await tester.tap(openNewScreenButton);
    await tester.pumpAndSettle();

    expect(find.text('Flutter screen 1'), findsOneWidget);
    expect(find.text('Item 1'), findsOneWidget);

    await tester.scrollUntilVisible(
      find.text('Item 20'),
      300,
      scrollable: find.byType(Scrollable),
    );
    expect(find.text('Item 20'), findsOneWidget);
  });
}

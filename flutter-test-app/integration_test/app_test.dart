import 'package:appium_flutter_server/appium_flutter_server.dart';
// import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test_sample/main.dart' as app;

void main() {
  // TestWidgetsFlutterBinding.ensureInitialized();
  initializeTest(app: const app.MyApp(title: "Flutter Test App"));
  // testWidgets('taps increment button and updates counter', (tester) async {
  //   app.main();
  //   await tester.pumpAndSettle();
  //
  //   expect(find.text('0'), findsOneWidget);
  //   expect(find.text('1'), findsNothing);
  //
  //   await tester.tap(find.byTooltip('Increment'));
  //   await tester.pumpAndSettle();
  //
  //   expect(find.text('0'), findsNothing);
  //   expect(find.text('1'), findsOneWidget);
  // });
}

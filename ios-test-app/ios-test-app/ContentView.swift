//
//  ContentView.swift
//  ios-test-app
//
//  Created by Lam Van on 5/3/2026.
//

import SwiftUI
import Flutter

struct ContentView: View {
    @ObservedObject var flutterDependencies: FlutterDependencies
    @State private var isFlutterPresented = false

    var body: some View {
        VStack {
            Button("Open Flutter app") {
                isFlutterPresented = true
            }
        }
        .padding()
        .fullScreenCover(isPresented: $isFlutterPresented) {
            FlutterScreenView(flutterEngine: flutterDependencies.flutterEngine)
                .ignoresSafeArea()
        }
    }
}

private struct FlutterScreenView: UIViewControllerRepresentable {
    let flutterEngine: FlutterEngine

    func makeUIViewController(context: Context) -> FlutterViewController {
        FlutterViewController(engine: flutterEngine, nibName: nil, bundle: nil)
    }

    func updateUIViewController(_ uiViewController: FlutterViewController, context: Context) {
    }
}

#Preview {
    ContentView(flutterDependencies: FlutterDependencies())
}

//
//  FlutterDependencies.swift
//  ios-test-app
//
//  Created by Cursor on 5/3/2026.
//

import Flutter
import FlutterPluginRegistrant
import Foundation

final class FlutterDependencies: ObservableObject {
    let flutterEngine: FlutterEngine

    init() {
        flutterEngine = FlutterEngine(name: "main flutter engine")
        flutterEngine.run()
        GeneratedPluginRegistrant.register(with: flutterEngine)
    }
}

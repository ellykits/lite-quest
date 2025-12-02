//
//  ContentView.swift
//  iosDemo
//
//  Created by Elly Kitoto on 30/11/2025.
//

import SwiftUI
import LiteQuestDemo

struct ComposeView: UIViewControllerRepresentable {
   func makeUIViewController(context: Context) -> UIViewController {
       MainViewControllerKt.MainViewController()
   }

   func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
   var body: some View {
       ComposeView()
           .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
   }
}

 // struct ContentView: View {
 //     var body: some View {
 //         VStack {
 //             Image(systemName: "globe")
 //                 .imageScale(.large)
 //                 .foregroundStyle(.tint)
 //             Text("Hello, world!")
 //         }
 //         .padding()
 //     }
 // }

#Preview {
    ContentView()
}

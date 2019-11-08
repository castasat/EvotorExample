package ru.castasat.evotor.example.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import ru.castasat.evotor.example.R
import ru.castasat.evotor.example.interactor.InternetReceiptInteractor
import ru.castasat.evotor.example.interactor.OpenReceiptInteractor

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOpenReceipt.setOnClickListener {
            OpenReceiptInteractor().execute(this)
        }

        btnInternetReceipt.setOnClickListener {
            InternetReceiptInteractor().execute(this)
        }
    }
}

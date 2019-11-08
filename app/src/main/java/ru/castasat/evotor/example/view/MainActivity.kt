package ru.castasat.evotor.example.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import ru.castasat.evotor.example.R
import ru.castasat.evotor.example.interactor.InternetReceiptInteractor

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnInternetReceipt.setOnClickListener {
            InternetReceiptInteractor().execute(this)
        }
    }
}

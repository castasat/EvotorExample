package ru.castasat.evotor.example.interactor

import android.app.Activity
import android.widget.Toast
import ru.evotor.framework.core.IntegrationException
import ru.evotor.framework.core.IntegrationManagerFuture
import ru.evotor.framework.core.IntegrationManagerFuture.Result
import ru.evotor.framework.core.IntegrationManagerFuture.Result.Type
import ru.evotor.framework.core.action.command.open_receipt_command.OpenSellReceiptCommand
import ru.evotor.framework.core.action.event.receipt.changes.position.PositionAdd
import ru.evotor.framework.navigation.NavigationApi.createIntentForSellReceiptEdit
import ru.evotor.framework.receipt.ExtraKey
import ru.evotor.framework.receipt.Position
import ru.evotor.framework.receipt.Position.Builder
import ru.evotor.framework.receipt.position.SettlementMethod.LoanPayment
import ru.castasat.evotor.example.BuildConfig
import java.math.BigDecimal
import java.util.*

class OpenReceiptInteractor {
    fun execute(activity: Activity) {
//        List<PositionAdd> positionAdds = new ArrayList<>();
//        positionAdds.add(new PositionAdd(createPosition(new BigDecimal(100), new BigDecimal(1))));
//        positionAdds.add(new PositionAdd(createPosition(new BigDecimal(200), new BigDecimal(2))));

        val list: MutableList<PositionAdd> =
            ArrayList()

        list.add(
            PositionAdd(
                Builder
                    .newInstance(
                        //UUID позиции
                        UUID.randomUUID().toString(),
                        //UUID товара
                        UUID.randomUUID().toString(),
                        //Наименование
                        "Тестовый",
                        //Наименование единицы измерения
                        "шт",
                        //Точность единицы измерения
                        0,
                        //Цена без скидок
                        BigDecimal(500),
                        //Количество
                        BigDecimal(1)
                    )
                    .setSettlementMethod(LoanPayment())
                    .build()
            )
        )
        OpenSellReceiptCommand(
            list,
            null
        ).process(
            activity
        ) { integrationManagerFuture: IntegrationManagerFuture ->
            try {
                val result: Result =
                    integrationManagerFuture.result
                if (result.type == Type.OK) {
                    activity.startActivity(createIntentForSellReceiptEdit())
                }
            } catch (e: IntegrationException) {
                e.printStackTrace()
                Toast.makeText(
                    activity,
                    "Есть проблема: " + e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun createPosition(
        price: BigDecimal?,
        quantity: BigDecimal?
    ): Position? {
        val extraKeys: MutableSet<ExtraKey> =
            HashSet()
        val uuid = UUID.randomUUID().toString()
        extraKeys.add(
            ExtraKey(
                uuid,
                BuildConfig.APP_UUID,
                "ExtraKey $uuid"
            )
        )
        return Builder.newInstance(
            UUID.randomUUID().toString(),
            PRODUCT_UUID,
            PRODUCT_NAME,
            MEASURE_NAME,
            0,
            price!!,
            quantity!!
        )
            .setExtraKeys(extraKeys)
            .build()
    }

    companion object {
        private val PRODUCT_UUID = UUID.randomUUID().toString()
        private const val PRODUCT_NAME = "Лучший товар на этой кассе"
        private const val MEASURE_NAME = "шт"
    }
}
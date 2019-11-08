package ru.castasat.evotor.example.service

import android.os.RemoteException
import ru.evotor.framework.core.IntegrationService
import ru.evotor.framework.core.action.event.receipt.before_positions_edited.BeforePositionsEditedEvent
import ru.evotor.framework.core.action.event.receipt.before_positions_edited.BeforePositionsEditedEventProcessor
import ru.evotor.framework.core.action.event.receipt.before_positions_edited.BeforePositionsEditedEventResult
import ru.evotor.framework.core.action.event.receipt.changes.position.IPositionChange
import ru.evotor.framework.core.action.event.receipt.changes.position.PositionAdd
import ru.evotor.framework.core.action.event.receipt.changes.position.PositionEdit
import ru.evotor.framework.core.action.processor.ActionProcessor
import ru.evotor.framework.receipt.ExtraKey
import ru.evotor.framework.receipt.Position
import ru.evotor.framework.receipt.Position.Builder
import ru.castasat.evotor.example.BuildConfig
import java.util.*

class BeforePositionsEdited : IntegrationService() {
    override fun createProcessors(): Map<String, ActionProcessor>? {
        val map: MutableMap<String, ActionProcessor> =
            HashMap()
        map[BeforePositionsEditedEvent.NAME_SELL_RECEIPT] = object :
            BeforePositionsEditedEventProcessor() {
            override fun call(
                action: String,
                event: BeforePositionsEditedEvent,
                callback: Callback
            ) {
                try {
                    callback.onResult(
                        BeforePositionsEditedEventResult(
                            process(event.changes),
                            null
                        )
                    )
//                    callback.skip();

                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }

            private fun process(changes: List<IPositionChange>): List<IPositionChange> {
                val result: MutableList<IPositionChange> =
                    ArrayList()
                for (positionChange in changes) {
                    var position: Position
                    if (positionChange is PositionAdd) {
                        position =
                            positionChange.position
                        val keys =
                            HashSet<ExtraKey>()
                        val extraUuid = UUID.randomUUID().toString()
                        keys.add(
                            ExtraKey(
                                extraUuid,
                                BuildConfig.APP_UUID,
                                "Экстра епт $extraUuid"
                            )
                        )
                        result.add(
                            PositionEdit(
                                Builder.copyFrom(position)
                                    .setExtraKeys(keys)
                                    .build()
                            )
                        )
                    } else if (positionChange is PositionEdit) {
                        position =
                            positionChange.position
                        val keys =
                            HashSet<ExtraKey>()
                        val extraUuid = UUID.randomUUID().toString()
                        keys.add(
                            ExtraKey(
                                extraUuid,
                                BuildConfig.APP_UUID,
                                "Экстра епт $extraUuid"
                            )
                        )
                        result.add(
                            PositionEdit(
                                Builder.copyFrom(position)
                                    .setExtraKeys(keys)
                                    .build()
                            )
                        )
                    } else {
                        result.add(positionChange)
                    }
                }
                return result
            }
        }
        return map
    }
}
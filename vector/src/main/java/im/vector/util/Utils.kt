package im.vector.util

import android.content.Context
import im.vector.VectorApp
import org.matrix.androidsdk.data.Room
import org.matrix.androidsdk.rest.model.RoomMember

fun filterRoomIn(directChats: List<Room> = emptyList(), list: List<KedrRoom> = emptyList()): List<Room> {
    return directChats.filter { chat -> list.find { chat.roomId == it.roomId } != null }
}

fun filterRoomOut(directChats: List<Room> = emptyList(), list: List<KedrRoom> = emptyList()): List<Room> {
    return directChats.filter { chat -> list.find { chat.roomId == it.roomId } == null }
}

fun filterCallsIn(calls: List<KedrCallHistory> = emptyList(), list: List<KedrRoom> = emptyList()): List<KedrCallHistory> {
    return calls.filter { call -> list.find { call.roomId == it.roomId } != null }
}

fun filterCallsOut(calls: List<KedrCallHistory> = emptyList(), list: List<KedrRoom> = emptyList()): List<KedrCallHistory> {
    return calls.filter { call -> list.find { call.roomId == it.roomId } == null }
}

fun isHome(context: Context): Boolean {
    return PreferencesManager.getDefaultPin(context) == VectorApp.currentPin
}

fun findDirectChatMember(chat: Room, myId: String): RoomMember? {
    return chat.members.find { member -> member.userId != myId }
}
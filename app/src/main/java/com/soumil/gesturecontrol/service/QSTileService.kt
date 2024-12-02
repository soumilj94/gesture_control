package com.soumil.gesturecontrol.service

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class QSTileService : TileService() {

    override fun onTileAdded() {
        super.onTileAdded()
        qsTile.state = Tile.STATE_INACTIVE
        qsTile.updateTile()
        qsTile.label = "Gesture Control"
    }

    override fun onClick() {
        super.onClick()

        val isActive = qsTile.state == Tile.STATE_INACTIVE
        qsTile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()

        val intent = Intent("com.soumil.gesturecontrol.TILE_STATE_CHANGED")
        intent.putExtra("isActive", isActive)
        sendBroadcast(intent)
    }
}
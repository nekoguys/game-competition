package ru.selemilka.game.core.rps.process

import ru.selemilka.game.core.rps.Session

class AnnouncementBuilder<Msg : RpsMessage> {
    val announcements = mutableListOf<RpsAnnouncement<Msg>>()

    infix fun Msg.to(player: RpsPlayer) {
        announcements += RpsAnnouncement.ToPlayer(player, this)
    }

    infix fun Msg.to(session: Session) {
        announcements += RpsAnnouncement.ToSession(session, this)
    }

    fun Msg.toAll() {
        announcements += RpsAnnouncement.ToAll(this)
    }
}

inline fun <Msg : RpsMessage> announces(
    block: AnnouncementBuilder<Msg>.() -> Unit,
): List<RpsAnnouncement<Msg>> =
    AnnouncementBuilder<Msg>()
        .apply(block)
        .announcements


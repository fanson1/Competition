package com.example.competition.repository.bridge

import com.example.competition.repository.ChallengeRepository
import com.example.competition.repository.LeaderboardRepository
import com.example.competition.repository.ModeRouter
import com.example.competition.repository.UserRepository
import com.example.competition.repository.local.LocalChallengeRepository
import com.example.competition.repository.local.LocalLeaderboardRepository
import com.example.competition.repository.local.LocalUserRepository
import com.example.competition.repository.remote.RemoteChallengeRepository
import com.example.competition.repository.remote.RemoteLeaderboardRepository
import com.example.competition.repository.remote.RemoteUserRepository

object RepositoryBridge {
    private var localUser: LocalUserRepository = LocalUserRepository()
    private var localLeaderboard: LocalLeaderboardRepository = LocalLeaderboardRepository()
    private var localChallenge: LocalChallengeRepository = LocalChallengeRepository()

    private var remoteUser: RemoteUserRepository = RemoteUserRepository()
    private var remoteLeaderboard: RemoteLeaderboardRepository = RemoteLeaderboardRepository()
    private var remoteChallenge: RemoteChallengeRepository = RemoteChallengeRepository()

    fun user(): UserRepository = resolve(remoteUser, localUser)
    fun leaderboard(): LeaderboardRepository = resolve(remoteLeaderboard, localLeaderboard)
    fun challenge(): ChallengeRepository = resolve(remoteChallenge, localChallenge)

    fun getRemoteUser(): RemoteUserRepository = remoteUser
    fun getRemoteLeaderboard(): RemoteLeaderboardRepository = remoteLeaderboard
    fun getRemoteChallenge(): RemoteChallengeRepository = remoteChallenge

    private fun <T> resolve(remote: T, local: T): T {
        return if (ModeRouter.isOnline()) remote else local
    }
}

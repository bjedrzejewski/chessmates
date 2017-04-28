package com.chessmates.lichess.data

import com.chessmates.repository.GameRepository
import com.chessmates.repository.MetaDataRepository
import com.chessmates.repository.MockGameRepository
import com.chessmates.repository.MockLichessMetaRepository
import com.chessmates.repository.MockPlayerRepository
import com.chessmates.repository.PlayerRepository
import com.chessmates.utility.HttpUtility
import com.google.common.base.Charsets
import com.google.common.collect.ImmutableMap
import com.google.common.io.Resources
import org.apache.commons.lang3.tuple.ImmutablePair
import org.spockframework.spring.ScanScopedBeans
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import spock.lang.Subject
import spock.mock.DetachedMockFactory

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE

@SpringBootTest(webEnvironment = NONE)
@ScanScopedBeans
class LichessDataServiceImplTest extends Specification {
    private static class Helper {

        static class MockEndpointInfo {
            String url
            String responseFile
        }

        static final PAGE_SIZE_GAMES = 10
        static final PAGE_SIZE_USERS = 5

        static final TEAM_WITH_EMPTY_USERS = new MockEndpointInfo(
                url: "https://en.lichess.org/api/user?team=scott-logic&nb=${PAGE_SIZE_USERS}&page=1",
                responseFile: 'scott-logic-users-empty.json'
        )
        static final TEAM_WITH_INVALID_USERS = new MockEndpointInfo(
                url: "https://en.lichess.org/api/user?team=scott-logic&nb=${PAGE_SIZE_USERS}&page=1",
                responseFile: 'scott-logic-users-invalid.json'
        )
        static final SCOTT_LOGIC_TEAM_1 = new MockEndpointInfo(
                url: "https://en.lichess.org/api/user?team=scott-logic&nb=${PAGE_SIZE_USERS}&page=1",
                responseFile: 'scott-logic-users-1.json'
        )
        static final SCOTT_LOGIC_TEAM_2 = new MockEndpointInfo(
                url: "https://en.lichess.org/api/user?team=scott-logic&nb=${PAGE_SIZE_USERS}&page=2",
                responseFile: 'scott-logic-users-2.json'
        )
        static final TF235_VS_OWENNW_INVALID = new MockEndpointInfo(
                url: "https://en.lichess.org/api/games/vs/tf235/owennw?nb=${PAGE_SIZE_GAMES}&page=1&with_analysis=1&with_moves=1&with_opening=1&with_movetimes=1&playing=0",
                responseFile: 'tf235-vs-owennw-invalid.json'
        )
        static final TF235_VS_OWENNW_1 = new MockEndpointInfo(
                url: "https://en.lichess.org/api/games/vs/tf235/owennw?nb=${PAGE_SIZE_GAMES}&page=1&with_analysis=1&with_moves=1&with_opening=1&with_movetimes=1&playing=0",
                responseFile: 'tf235-vs-owennw-1.json'
        )
        static final TF235_VS_OWENNW_2 = new MockEndpointInfo(
                url: "https://en.lichess.org/api/games/vs/tf235/owennw?nb=${PAGE_SIZE_GAMES}&page=2&with_analysis=1&with_moves=1&with_opening=1&with_movetimes=1&playing=0",
                responseFile: 'tf235-vs-owennw-2.json'
        )
        static final TF235_VS_JEDRUS07_1 = new MockEndpointInfo(
                url: "https://en.lichess.org/api/games/vs/tf235/jedrus07?nb=${PAGE_SIZE_GAMES}&page=1&with_analysis=1&with_moves=1&with_opening=1&with_movetimes=1&playing=0",
                responseFile: 'tf235-vs-jedrus07-1.json'
        )
        static final TF235_VS_JEDRUS07_2 = new MockEndpointInfo(
                url: "https://en.lichess.org/api/games/vs/tf235/jedrus07?nb=${PAGE_SIZE_GAMES}&page=2&with_analysis=1&with_moves=1&with_opening=1&with_movetimes=1&playing=0",
                responseFile: 'tf235-vs-jedrus07-2.json'
        )
        static final OWENNW_VS_JEDRUS07_1 = new MockEndpointInfo(
                url: "https://en.lichess.org/api/games/vs/owennw/jedrus07?nb=${PAGE_SIZE_GAMES}&page=1&with_analysis=1&with_moves=1&with_opening=1&with_movetimes=1&playing=0",
                responseFile: 'owennw-vs-jedrus07-1.json'
        )
        static final OWENNW_VS_JEDRUS07_2 = new MockEndpointInfo(
                url: "https://en.lichess.org/api/games/vs/owennw/jedrus07?nb=${PAGE_SIZE_GAMES}&page=2&with_analysis=1&with_moves=1&with_opening=1&with_movetimes=1&playing=0",
                responseFile: 'owennw-vs-jedrus07-2.json'
        )

        static String loadFile(String fileName) {
            Resources.toString(Resources.getResource(fileName), Charsets.UTF_8)
        }

        static boolean entityIs(id, entity) { entity.id == id }

    }

    @Subject
    LichessDataServiceImpl service

    LichessApi api

    HttpUtility httpUtility

    PlayerRepository playerRepository

    GameRepository gameRepository

    MetaDataRepository metaDataRepository

    def setup() {
        api = new LichessApiImpl()
        httpUtility = Mock(HttpUtility)
        playerRepository = Mock(PlayerRepository)
        gameRepository = Mock(GameRepository)
        metaDataRepository = Mock(MetaDataRepository)

        api.httpUtility = httpUtility

        service = new LichessDataServiceImpl(api, playerRepository, gameRepository, metaDataRepository)

        // Set smaller page sizes as we provide data for these pages sizes.
        ReflectionTestUtils.setField(service, 'pageSizePlayers', Helper.PAGE_SIZE_USERS)
        ReflectionTestUtils.setField(service, 'pageSizeGames', Helper.PAGE_SIZE_GAMES)
    }

    def noLatestGames() {
        // The MetaDataRepository should return an empty store by default.
        metaDataRepository.latestGames >> ImmutableMap.builder().build()
    }

    // Player tests.

    def "ignores invalid players"() {
        given:
        httpUtility.get(Helper.TEAM_WITH_INVALID_USERS.url) >> Helper.loadFile(Helper.TEAM_WITH_INVALID_USERS.responseFile)

        when:
        final players = service.updatePlayers()

        then:
        players.size() == 1
    }

    def "returns empty list of players with empty data set"() {
        given:
        httpUtility.get(Helper.TEAM_WITH_EMPTY_USERS.url) >> Helper.loadFile(Helper.TEAM_WITH_EMPTY_USERS.responseFile)

        when:
        final players = service.updatePlayers()

        then:
        players.size() == 0
    }

    def "returns full set of players when no latest player is provided"() {
        given:
        httpUtility.get(Helper.SCOTT_LOGIC_TEAM_1.url) >> Helper.loadFile(Helper.SCOTT_LOGIC_TEAM_1.responseFile)
        httpUtility.get(Helper.SCOTT_LOGIC_TEAM_2.url) >> Helper.loadFile(Helper.SCOTT_LOGIC_TEAM_2.responseFile)

        when:
        final players = service.updatePlayers()

        then:
        players.size() == 8
        players[0].id == 'jfaker'
        players[1].id == 'riciardos'
        players[2].id == 'samei07'
        players[3].id == 'sydeman'
        players[4].id == 'torrlane'
        players[5].id == 'tf235'
        players[6].id == 'owennw'
        players[7].id == 'jedrus07'
    }

    def "stops fetching at provided player when latest player is provided"() {
        given:
        metaDataRepository.getLatestPlayer() >> [id: 'tf235']

        and:
        httpUtility.get(Helper.SCOTT_LOGIC_TEAM_1.url) >> Helper.loadFile(Helper.SCOTT_LOGIC_TEAM_1.responseFile)
        httpUtility.get(Helper.SCOTT_LOGIC_TEAM_2.url) >> Helper.loadFile(Helper.SCOTT_LOGIC_TEAM_2.responseFile)

        when:
        final players = service.updatePlayers()

        then:
        players.size() == 5
        players[0].id == 'jfaker'
        players[1].id == 'riciardos'
        players[2].id == 'samei07'
        players[3].id == 'sydeman'
        players[4].id == 'torrlane'
    }

    def "saves players when new player is fetched"() {
        given:
        metaDataRepository.getLatestPlayer() >> [id: 'tf235']

        and:
        httpUtility.get(Helper.SCOTT_LOGIC_TEAM_1.url) >> Helper.loadFile(Helper.SCOTT_LOGIC_TEAM_1.responseFile)
        httpUtility.get(Helper.SCOTT_LOGIC_TEAM_2.url) >> Helper.loadFile(Helper.SCOTT_LOGIC_TEAM_2.responseFile)

        when:
        service.updatePlayers()

        then:
        1 * playerRepository.saveAll(
                {
                    it[0].id == "jfaker" &&
                    it[1].id == "riciardos" &&
                    it[2].id == "samei07" &&
                    it[3].id == "sydeman" &&
                    it[4].id == "torrlane"
                })
    }

    def "saves latest player when players are fetched"() {
        given:
        httpUtility.get(Helper.SCOTT_LOGIC_TEAM_1.url) >> Helper.loadFile(Helper.SCOTT_LOGIC_TEAM_1.responseFile)
        httpUtility.get(Helper.SCOTT_LOGIC_TEAM_2.url) >> Helper.loadFile(Helper.SCOTT_LOGIC_TEAM_2.responseFile)



        when:
        service.updatePlayers()

        then:
        1 * metaDataRepository.saveLatestPlayer({ it.id == 'jfaker' })
    }

    // Games tests.

    def "ignores invalid games"() {
        given:
        final players = [
                [id: 'tf235'],
                [id: 'owennw']
        ]

        and:
        noLatestGames()
        httpUtility.get(Helper.TF235_VS_OWENNW_INVALID.url) >> Helper.loadFile(Helper.TF235_VS_OWENNW_INVALID.responseFile)

        when:
        final games = service.updateGames(players)

        then:
        games.size() == 1
    }

    def "returns empty games when no players are provided"() {
        given:
        final players = null

        and:
        noLatestGames()

        when:
        final games = service.updateGames(players)

        then:
        games.size() == 0
    }

    def "returns full set of games when no latest games are provided"() {
        given:
        final players = [
                [id: 'tf235'],
                [id: 'owennw'],
                [id: 'jedrus07']
        ]

        and:
        noLatestGames()
        httpUtility.get(Helper.TF235_VS_OWENNW_1.url) >> Helper.loadFile(Helper.TF235_VS_OWENNW_1.responseFile)
        httpUtility.get(Helper.TF235_VS_OWENNW_2.url) >> Helper.loadFile(Helper.TF235_VS_OWENNW_2.responseFile)
        httpUtility.get(Helper.TF235_VS_JEDRUS07_1.url) >> Helper.loadFile(Helper.TF235_VS_JEDRUS07_1.responseFile)
        httpUtility.get(Helper.TF235_VS_JEDRUS07_2.url) >> Helper.loadFile(Helper.TF235_VS_JEDRUS07_2.responseFile)
        httpUtility.get(Helper.OWENNW_VS_JEDRUS07_1.url) >> Helper.loadFile(Helper.OWENNW_VS_JEDRUS07_1.responseFile)
        httpUtility.get(Helper.OWENNW_VS_JEDRUS07_2.url) >> Helper.loadFile(Helper.OWENNW_VS_JEDRUS07_2.responseFile)

        when:
        final games = service.updateGames(players)

        then:
        games.size() == 56
    }

    def "stops fetching at provided game when latest games are provided"() {
        given:
        final players = [
                [id: 'tf235'],
                [id: 'owennw'],
                [id: 'jedrus07']
        ]

        and:
        metaDataRepository.getLatestGames() >> ImmutableMap.builder().putAll([
                (new ImmutablePair(players[0], players[1])): [id: 'tlicb8yX'],
                (new ImmutablePair(players[0], players[2])): [id: 'ThSBEyjg'],
                (new ImmutablePair(players[1], players[2])): [id: '0Qk6CAqq']
        ]).build()

        and:
        httpUtility.get(Helper.TF235_VS_OWENNW_1.url) >> Helper.loadFile(Helper.TF235_VS_OWENNW_1.responseFile)
        httpUtility.get(Helper.TF235_VS_OWENNW_2.url) >> Helper.loadFile(Helper.TF235_VS_OWENNW_2.responseFile)
        httpUtility.get(Helper.TF235_VS_JEDRUS07_1.url) >> Helper.loadFile(Helper.TF235_VS_JEDRUS07_1.responseFile)
        httpUtility.get(Helper.TF235_VS_JEDRUS07_2.url) >> Helper.loadFile(Helper.TF235_VS_JEDRUS07_2.responseFile)
        httpUtility.get(Helper.OWENNW_VS_JEDRUS07_1.url) >> Helper.loadFile(Helper.OWENNW_VS_JEDRUS07_1.responseFile)
        httpUtility.get(Helper.OWENNW_VS_JEDRUS07_2.url) >> Helper.loadFile(Helper.OWENNW_VS_JEDRUS07_2.responseFile)

        when:
        final games = service.updateGames(players)

        then:
        games.size() == 18
    }

    def "saves game when new game is fetched"() {
        given:
        final players = [
                [id: 'tf235'],
                [id: 'owennw'],
                [id: 'jedrus07']
        ]

        and:
        noLatestGames()
        httpUtility.get(Helper.TF235_VS_OWENNW_1.url) >> Helper.loadFile(Helper.TF235_VS_OWENNW_1.responseFile)
        httpUtility.get(Helper.TF235_VS_OWENNW_2.url) >> Helper.loadFile(Helper.TF235_VS_OWENNW_2.responseFile)
        httpUtility.get(Helper.TF235_VS_JEDRUS07_1.url) >> Helper.loadFile(Helper.TF235_VS_JEDRUS07_1.responseFile)
        httpUtility.get(Helper.TF235_VS_JEDRUS07_2.url) >> Helper.loadFile(Helper.TF235_VS_JEDRUS07_2.responseFile)
        httpUtility.get(Helper.OWENNW_VS_JEDRUS07_1.url) >> Helper.loadFile(Helper.OWENNW_VS_JEDRUS07_1.responseFile)
        httpUtility.get(Helper.OWENNW_VS_JEDRUS07_2.url) >> Helper.loadFile(Helper.OWENNW_VS_JEDRUS07_2.responseFile)

        when:
        service.updateGames(players)

        then:
        1 * gameRepository.saveAll({it.size() == 20})
        1 * gameRepository.saveAll({it.size() == 20})
        1 * gameRepository.saveAll({it.size() == 16})
    }

    def "saves latest game for each player when games are fetched"() {
        given:
        final players = [
                [id: 'tf235'],
                [id: 'owennw'],
                [id: 'jedrus07']
        ]

        and:
        noLatestGames()
        httpUtility.get(Helper.TF235_VS_OWENNW_1.url) >> Helper.loadFile(Helper.TF235_VS_OWENNW_1.responseFile)
        httpUtility.get(Helper.TF235_VS_OWENNW_2.url) >> Helper.loadFile(Helper.TF235_VS_OWENNW_2.responseFile)
        httpUtility.get(Helper.TF235_VS_JEDRUS07_1.url) >> Helper.loadFile(Helper.TF235_VS_JEDRUS07_1.responseFile)
        httpUtility.get(Helper.TF235_VS_JEDRUS07_2.url) >> Helper.loadFile(Helper.TF235_VS_JEDRUS07_2.responseFile)
        httpUtility.get(Helper.OWENNW_VS_JEDRUS07_1.url) >> Helper.loadFile(Helper.OWENNW_VS_JEDRUS07_1.responseFile)
        httpUtility.get(Helper.OWENNW_VS_JEDRUS07_2.url) >> Helper.loadFile(Helper.OWENNW_VS_JEDRUS07_2.responseFile)

        when:
        service.updateGames(players)

        then:
        1 * metaDataRepository.saveLatestGame({ it.id == 'tf235' }, { it.id == 'owennw' }, { it.id == 'OBBHfGOC' })
        1 * metaDataRepository.saveLatestGame({ it.id == 'tf235' }, { it.id == 'jedrus07' }, { it.id == 'uP0rXPYL' })
        1 * metaDataRepository.saveLatestGame({ it.id == 'owennw' }, { it.id == 'jedrus07' }, { it.id == '1J73NgR1' })
    }

    def "games are sorted by last move"() {
        given:
        final players = [
                [id: 'tf235'],
                [id: 'owennw'],
                [id: 'jedrus07']
        ]

        and:
        noLatestGames()
        httpUtility.get(Helper.TF235_VS_OWENNW_1.url) >> Helper.loadFile(Helper.TF235_VS_OWENNW_1.responseFile)
        httpUtility.get(Helper.TF235_VS_OWENNW_2.url) >> Helper.loadFile(Helper.TF235_VS_OWENNW_2.responseFile)
        httpUtility.get(Helper.TF235_VS_JEDRUS07_1.url) >> Helper.loadFile(Helper.TF235_VS_JEDRUS07_1.responseFile)
        httpUtility.get(Helper.TF235_VS_JEDRUS07_2.url) >> Helper.loadFile(Helper.TF235_VS_JEDRUS07_2.responseFile)
        httpUtility.get(Helper.OWENNW_VS_JEDRUS07_1.url) >> Helper.loadFile(Helper.OWENNW_VS_JEDRUS07_1.responseFile)
        httpUtility.get(Helper.OWENNW_VS_JEDRUS07_2.url) >> Helper.loadFile(Helper.OWENNW_VS_JEDRUS07_2.responseFile)

        when:
        final games = service.updateGames(players)

        then:
        // Test newest three games (all tf235 vs jedrus07)
        games[0].id == 'uP0rXPYL'
        games[1].id == 'wRJFs6zn'
        games[2].id == 'UxkkULFV'

        // Test newest of owennw vs tf235
        games[8].id == 'OBBHfGOC'

        // Test newest of owennw vs jedrus
        games[9].id == '1J73NgR1'
    }

}

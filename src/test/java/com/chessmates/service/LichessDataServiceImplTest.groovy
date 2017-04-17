package com.chessmates.service

import com.chessmates.model.Player
import com.chessmates.utility.HttpUtility
import groovy.json.JsonBuilder
import org.codehaus.groovy.tools.shell.commands.HelpCommand
import org.spockframework.spring.ScanScopedBeans
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import spock.lang.Specification
import spock.lang.Subject
import spock.mock.DetachedMockFactory

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE

@SpringBootTest(webEnvironment = NONE)
@ScanScopedBeans
class LichessDataServiceImplTest extends Specification {

    @Subject
    @Autowired
    LichessDataService service

    @Autowired
    HttpUtility httpUtility

    def "returns empty players when no players provided"() {
        when:
        final players = service.getPlayers(null)

        then:
        httpUtility.get(_) >> Helper.emptyPlayers()
        players.size() == 0
    }

    def "returns full set of players when no latest player is provided"() {
        given:
        final threePages = 3
        final ofTenPlayers = 10
        final pageGenerator = Helper.pagesOfPlayers(threePages, ofTenPlayers)

        when:
        final players = service.getPlayers(null)

        then:
        3 * httpUtility.get(_) >>> [ pageGenerator(1), pageGenerator(2), pageGenerator(3)]

        players.size() == 30
        Helper.verifyContigiousPlayers(players)
    }

    def "stops fetching at provided player when latest player is provided"() {
        given:
        final threePages = 3
        final ofTenPlayers = 10
        final pageGenerator = Helper.pagesOfPlayers(threePages, ofTenPlayers)
        final latestPlayer = 'player19'

        when:
        final players = service.getPlayers(latestPlayer)

        then:
        2 * httpUtility.get(_) >>> [ pageGenerator(1), pageGenerator(2)]
        players.size() == 18
        Helper.verifyContigiousPlayers(players)
    }

    private static class Helper {

        static String emptyPlayers() {
            def builder = new JsonBuilder()
            builder.paginator {
                currentPage 1
                currentPageResults([])
                maxPerPage 10
                nbPages 1
                nbResults 0
                nextPage null
                previousPage null
            }

            builder.toString()
        }

        /** Given a number of pages & page size, returns a function that generates mock pages of that dataset. */
        static Closure<String> pagesOfPlayers(int numPages, int pageSize) {
            // Calculate other meta for data set
            final Integer numberOfResults = pageSize * numPages
            final pageRange = 1..numPages

            return { Integer pageNum ->
                // Create players for this page.
                final pageOutOfRange = pageRange.contains(pageNum)
                def players
                if (pageOutOfRange) {
                    final pagesPlayers = ((pageSize * (pageNum - 1)) + 1)..(pageSize * pageNum)
                    players = pagesPlayers.collect { [id: "player${it}", username: "player${it}"] }
                }

                // Replicates Lichess behaviour, requests for pages less than 1 just return 1.
                pageNum = Math.max(1, pageNum)

                // Replicates Lichess behaviour. Pages over the num pages return data set with null next page but populated previous.
                final Integer nextPageNum = pageNum >= numPages ? null : pageNum + 1
                final Integer previousPageNum = pageNum == 1 ? null : pageNum - 1

                // Build the JSON!
                final builder = new JsonBuilder()
                builder.paginator {
                    currentPage pageNum
                    currentPageResults players
                    maxPerPage pageSize
                    nbPages numPages
                    nbResults numberOfResults
                    nextPage nextPageNum
                    previousPage previousPageNum
                }
                builder.toString()
            }
        }

        /** As test data here names players from player1 to playerN, we can verifiy correct parsing by checking they
         * match their place in the array +1. */
        static boolean verifyContigiousPlayers(List<Player> players) {
            players.eachWithIndex { player, index ->
                assert player.id == "player${index+1}".toString()
                assert player.username == "player${index+1}".toString()
            }
        }
    }

    @TestConfiguration
    private static class MockConfig {

        def detatchedMockFactory = new DetachedMockFactory()

        @Bean
        HttpUtility httpUtility() {
            return detatchedMockFactory.Mock(HttpUtility)
        }

    }

}

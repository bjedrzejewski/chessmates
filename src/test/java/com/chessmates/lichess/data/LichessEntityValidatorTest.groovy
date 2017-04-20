package com.chessmates.lichess.data

import spock.lang.Specification


class LichessEntityValidatorTest extends Specification {

    def "item without id is invalid"() {
        given:
        final item = [otherProp: '']

        expect:
        assert !LichessEntityValidator.isValid(item)
    }

    def "item with empty id is invalid"() {
        given:
        final item = [id: '']

        expect:
        assert !LichessEntityValidator.isValid(item)
    }

    def "item with id is valid"() {
        given:
        final item = [id: 'asdf']

        expect:
        assert LichessEntityValidator.isValid(item)
    }

}

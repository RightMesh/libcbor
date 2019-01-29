package io.left.rightmesh.libcbor.parser.states;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.parser.CborParser;
import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborOr extends ParserState {

    ByteBuffer[] buf;
    CborParser[] p;
    int[] status; // 0 = can continue, 1 = failed, 2 = success
    int nb_0 = 0;
    int nb_1 = 0;
    int nb_2 = 0;
    int winner;

    public CborOr(CborParser[] parsers) {
        buf = new ByteBuffer[parsers.length];
        status = new int[parsers.length];
        for (int i = 0; i < parsers.length; i++) {
            status[i] = 0;
            nb_0++;
        }
        p = parsers;
    }


    void parser_success(int i) {
        status[i] = 2;
        nb_0--;
        nb_2++;
        winner = i; // only works if one winner only
    }

    void parser_failed(int i) {
        status[i] = 1;
        nb_0--;
        nb_1++;
    }

    @Override
    public ParserState onNext(ByteBuffer next) throws RxParserException {
        for (int i = 0; i < p.length; i++) {
            buf[i] = next.duplicate();
        }

        for (int i = 0; i < p.length; i++) {
            // if still valid
            if (status[i] == 0) {
                try {
                    if (p[i].read(buf[i])) {
                        // success parsing
                        parser_success(i);
                    }
                } catch (RxParserException rpe) {
                    // failed because of parser exception
                    parser_failed(i);
                }
            }
        }

        if (nb_1 == p.length) {
            throw new RxParserException("CborOr", "All parser from disjunction failed");
        }
        if (nb_2 > 1) {
            throw new RxParserException("CborOr", "Ambiguous disjunction, multiple success");
        }
        if (nb_2 == 1) {
            next.position(buf[winner].position());
            return onSuccess(p[winner]);
        }

        // nb 2 == 0 and nb_1 is not full so there are a few contender left
        // that needs more buffer
        next.position(next.limit());
        return this;
    }

    public abstract ParserState onSuccess(CborParser p);
}

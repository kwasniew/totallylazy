package com.googlecode.totallylazy;

public abstract class Curried4<A, B, C, D, E> extends Curried3<A, B, C, Function1<D, E>> implements Function4<A, B, C, D, E> {
    @Override
    public Function1<D, E> call(final A a, final B b, final C c) throws Exception {
        return Functions.<A, B, C, D, E>apply(this, a).apply(b).apply(c);
    }

    public E apply(final A a, final B b, final C c, final D d){
        return Functions.call(this, a, b, c, d);
    }

    public Function1<Quadruple<A, B, C, D>, E> quadruple() {
        return Functions.quadruple(this);
    }

}
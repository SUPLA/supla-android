package org.supla.android.extensions
/*
Copyright (C) AC SOFTWARE SP. Z O.O.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

fun <R : Any, T : Any> Maybe<R>.flatMapMerged(otherProducer: (R) -> Maybe<T>): Maybe<Pair<R, T>> {
  return flatMap { sourceItem -> otherProducer(sourceItem).map { otherItem -> Pair(sourceItem, otherItem) } }
}

fun <R : Any, T : Any> Maybe<R>.mapMerged(otherProducer: (R) -> T): Maybe<Pair<R, T>> {
  return map { sourceItem -> Pair(sourceItem, otherProducer(sourceItem)) }
}

fun <R : Any, T : Any> Observable<R>.flatMapMerged(otherProducer: (R) -> Observable<T>): Observable<Pair<R, T>> {
  return flatMap { sourceItem -> otherProducer(sourceItem).map { otherItem -> Pair(sourceItem, otherItem) } }
}

fun <R : Any, T> Observable<R>.mapMerged(otherProducer: (R) -> T): Observable<Pair<R, T>> {
  return map { sourceItem -> Pair(sourceItem, otherProducer(sourceItem)) }
}

fun <R : Any, T : Any> Single<R>.flatMapMerged(otherProducer: (R) -> Single<T>): Single<Pair<R, T>> {
  return flatMap { sourceItem -> otherProducer(sourceItem).map { otherItem -> Pair(sourceItem, otherItem) } }
}

fun <R : Any, T> Single<R>.mapMerged(otherProducer: (R) -> T): Single<Pair<R, T>> {
  return map { sourceItem -> Pair(sourceItem, otherProducer(sourceItem)) }
}

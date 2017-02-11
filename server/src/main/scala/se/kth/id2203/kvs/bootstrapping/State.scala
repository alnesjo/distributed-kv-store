package se.kth.id2203.kvs.bootstrapping

sealed trait State

case object Collecting extends State

case object Seeding extends State

case object Done extends State
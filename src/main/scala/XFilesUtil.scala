// See LICENSE for license details

package xfiles

import Chisel._

class QueueIOAf[T <: Data](gen: T, entries: Int) extends QueueIO[T](gen, entries) {
  val almostFull = Bool(OUTPUT)
  override def cloneType = new QueueIOAf(gen, entries).asInstanceOf[this.type]
}

class QueueAf[T <: Data](gen: T, entries: Int, almostFullEntries: Int,
  pipe: Boolean = false, flow: Boolean = false, _reset: Option[Bool] = None)
    extends Module(_reset = _reset) {

  val io = new QueueIOAf(gen, entries)
  val queue = Module(new Queue(gen, entries, pipe, flow, _reset))

  io <> queue.io
  io.almostFull := queue.io.count >= UInt(almostFullEntries)
}

// See LICENSE for license details.

package dana

import Chisel._

import rocket._
import cde.{Parameters, Field}

class TransactionState(implicit p: Parameters) extends XFilesBundle()(p) {
  //-------- Base class
  val valid = Bool()
  val reserved = Bool()
  val cacheValid = Bool()
  val waiting = Bool()
  val needsLayerInfo = Bool()
  val done = Bool()
  val decInUse = Bool()
  // There are two "in the last layer" bits. The first, "inLast",
  // asserts when all PEs in the previous layer are done. The latter,
  // "inLastEarly", asserts as soon as all PEs in the previous layer
  // have been assigned.
  val inLast = Bool()
  val inFirst = Bool()
  val cacheIndex = UInt(width = log2Up(cacheNumEntries))
  val asid = UInt(width = asidWidth)
  val tid = UInt(width = tidWidth)
  val nnid = UInt(width = nnidWidth)
  val decimalPoint = UInt(width = decimalPointWidth)
  val numLayers = UInt(width = 16) // [TODO] fragile
  val numNodes = UInt(width = 16) // [TODO] fragile
  val currentNode = UInt(width = 16) // [TODO] fragile
  val currentNodeInLayer = UInt(width = 16) // [TODO] fragile
  val currentLayer = UInt(width = 16) // [TODO] fragile
  val nodesInCurrentLayer = UInt(width = 16) // [TODO] fragile
  val neuronPointer = UInt(width = 11) // [TODO] fragile
  val regFileLocationBit = UInt(width = 1)
  val regFileAddrIn = UInt(width = log2Up(regFileNumElements))
  val regFileAddrOut = UInt(width = log2Up(regFileNumElements))
  val readIdx = UInt(width = log2Up(regFileNumElements))
  val coreIdx = UInt(width = log2Up(numCores))
  val indexElement = UInt(width = log2Up(regFileNumElements))
  val countFeedback = UInt(width = feedbackWidth)
  //-------- Can be possibly moved over to a learning-only config
  val regFileAddrOutFixed = UInt(width = log2Up(regFileNumElements))

  aliasList += ( "valid" -> "V",
    "reserved" -> "R",
    "cacheValid" -> "C",
    "waiting" -> "W",
    "needsLayerInfo" -> "NLI",
    "done" -> "D",
    "decInUse" -> "-",
    "inLast" -> "L?",
    "inFirst" -> "F?",
    "cacheIndex" -> "C#",
    "decimalPoint" -> "DP",
    "numLayers" -> "#L",
    "numNodes" -> "#N",
    "currentNode" -> "cN",
    "currentNodeInLayer" -> "cNiL",
    "currentLayer" -> "cL",
    "nodesInCurrentLayer" -> "#NcL",
    "neuronPointer" -> "N*",
    "regFileLocationBit" -> "LB",
    "regFileAddrIn" -> "AIn",
    "regFileAddrOut" -> "AOut",
    "readIdx" -> "R#",
    "coreIdx" -> "Co#",
    "indexElement" -> "#E",
    "countFeedback" -> "CF",
    "regFileAddrOutFixed" -> "AOutF"
  )
}

class TransactionStateLearn(implicit p: Parameters)
    extends TransactionState()(p) {
  val globalWtptr = UInt(width = 16) //[TODO] fragile
  val inLastEarly = Bool()
  val transactionType = UInt(width = log2Up(3)) // [TODO] fragile
  val numTrainOutputs = UInt(width = 16) // [TODO] fragile
  val stateLearn = UInt(width = log2Up(7)) // [TODO] fragile
  val errorFunction = UInt(width = log2Up(2)) // [TODO] fragile
  val learningRate = UInt(width = 16) // [TODO] fragile
  val lambda = UInt(width = 16) // [TODO] fragile
  val numWeightBlocks = UInt(width = 16) // [TODO] fragile
  val mse = UInt(width = elementWidth) // unused
  // Batch training information
  val numBatchItems = UInt(width = 16) // [TODO] fragile
  val curBatchItem = UInt(width = 16) // [TODO] fragile
  val biasAddr = UInt(width = 16) // [TODO] fragile
  val offsetBias = UInt(width = 16) // [TODO] fragile
  val offsetDW = UInt(width = 16) // [TODO] fragile
  // We need to keep track of where inputs and outputs should be
  // written to in the Register File.
  val regFileAddrInFixed = UInt(width = log2Up(regFileNumElements))
  // val regFileAddrDelta = UInt(width = log2Up(regFileNumElements))
  val regFileAddrDW = UInt(width = log2Up(regFileNumElements))
  val regFileAddrSlope = UInt(width = log2Up(regFileNumElements))
  val regFileAddrAux = UInt(width = log2Up(regFileNumElements))
  val nodesInPreviousLayer = UInt(width = 16) // [TODO] fragile
  val nodesInLast = UInt(width = 16) // [TODO] fragile

  aliasList += (
    "globalWtptr" -> "GW*",
    "inLastEarly" -> "L?e",
    "transactionType" -> "T?",
    "numTrainOutputs" -> "#TO",
    "stateLearn" -> "state",
    "errorFunction" -> "ef",
    "learningRate" -> "lr",
    "lambda" -> "Y",
    "numWeightBlocks" -> "#WB",
    "numBatchItems" -> "#BI",
    "curBatchItem" -> "cB",
    "biasAddr" -> "AB",
    "offsetBias" -> "oB",
    "offsetDW" -> "oDW",
    "regFileAddrInFixed" -> "AInF",
    "regFileAddrDW" -> "ADW",
    "regFileAddrSlope" -> "AS",
    "regFileAddrAux" -> "AAux",
    "nodesInPreviousLayer" -> "nipl",
    "nodesInLast" -> "nil"
  )
}

class ControlReq(implicit p: Parameters) extends XFilesBundle()(p) {
  // Bools
  val cacheValid = Bool()
  val waiting = Bool()
  val needsLayerInfo = Bool()
  val isDone = Bool()
  val inFirst = Bool()
  val inLast = Bool()
  // Global info
  val tableIndex = UInt(width = log2Up(transactionTableNumEntries))
  val cacheIndex = UInt(width = log2Up(cacheNumEntries))
  val asid = UInt(width = asidWidth)
  val nnid = UInt(width = nnidWidth) // formerly nn_hash
  val coreIdx = UInt(width = log2Up(numCores))
  // State info
  val currentNodeInLayer = UInt(width = 16) // [TODO] fragile
  val currentLayer = UInt(width = 16) // [TODO] fragile
  val neuronPointer = UInt(width = 11) // [TODO] fragile
  val decimalPoint = UInt(width = decimalPointWidth)
  val regFileAddrIn = UInt(width = log2Up(regFileNumElements))
  val regFileAddrOut = UInt(width = log2Up(regFileNumElements))
  val regFileLocationBit = UInt(width = 1) // [TODO] fragile on definition above
}

class ControlReqLearn(implicit p: Parameters) extends ControlReq()(p) {
  val globalWtptr = UInt(width = 16) // [TODO] fragile
  val inLastEarly = Bool()
  val transactionType = UInt(width = log2Up(3)) // [TODO] fragile
  val stateLearn = UInt(width = log2Up(5)) // [TODO] fragile
  val errorFunction = UInt(width = log2Up(2)) // [TODO] fragile
  val learningRate = UInt(width = 16) // [TODO] fragile
  val lambda = UInt(width = 16) // [TODO] fragile
  val numWeightBlocks = UInt(width = 16) // [TODO] fragile
  // val regFileAddrDelta = UInt(width = log2Up(regFileNumElements))
  val regFileAddrDW = UInt(width = log2Up(regFileNumElements))
  val regFileAddrSlope = UInt(width = log2Up(regFileNumElements))
  val regFileAddrBias = UInt(width = log2Up(regFileNumElements))
  val regFileAddrAux = UInt(width = log2Up(regFileNumElements))
  val batchFirst = Bool()
}

class ControlResp(implicit p: Parameters) extends XFilesBundle()(p) {
  val readyCache = Bool()
  val readyPeTable = Bool()
  val cacheValid = Bool()
  val tableIndex = UInt(width = log2Up(transactionTableNumEntries))
  val field = UInt(width = 4) // [TODO] fragile on Constants.scala
  val data = Vec.fill(6){UInt(width = 16)} // [TODO] fragile
  val decimalPoint = UInt(width = decimalPointWidth)
  val layerValid = Bool()
  val layerValidIndex = UInt(width = log2Up(transactionTableNumEntries))
}

class ControlRespLearn(implicit p: Parameters) extends ControlResp()(p) {
  val globalWtptr = UInt(width = 16) //[TODO] fragile
}

class XFilesArbiterRespPipe(implicit p: Parameters) extends XFilesBundle()(p) {
  val respType = UInt(width = log2Up(3)) // [TODO] Fragile on Dana enum
  val tid = UInt(width = tidWidth)
  val tidIdx = UInt(width = log2Up(transactionTableNumEntries))
  val readIdx = UInt(width = log2Up(regFileNumElements))
  val coreIdx = UInt(width = log2Up(numCores))
  val rd = UInt(width = 5) // Dependent on rocc.scala defined width
  val status = UInt(width = elementWidth)
}

class TTableControlInterface(implicit p: Parameters) extends Bundle {
  lazy val req = Decoupled(new ControlReq)
  lazy val resp = Decoupled(new ControlResp).flip
}

class TTableControlInterfaceLearn(implicit p: Parameters)
    extends TTableControlInterface()(p) {
  override lazy val req = Decoupled(new ControlReqLearn)
  override lazy val resp = Decoupled(new ControlRespLearn).flip
}

class TTableRegisterFileReq(implicit p: Parameters) extends XFilesBundle()(p) {
  val reqType = UInt(width = log2Up(2)) // [TODO] Frgaile on Dana enum
  val tidIdx = UInt(width = log2Up(transactionTableNumEntries))
  val addr = UInt(width = log2Up(regFileNumElements))
  val data = UInt(width = elementWidth)
}

class TTableRegisterFileResp(implicit p: Parameters) extends XFilesBundle()(p) {
  val data = UInt(width = elementWidth)
}

class TTableRegisterFileInterface(implicit p: Parameters) extends XFilesBundle()(p) {
  val req = Valid(new TTableRegisterFileReq)
  val resp = Valid(new TTableRegisterFileResp).flip
}

class TransactionTableInterface(implicit p: Parameters) extends XFilesBundle()(p) {
  val arbiter = new XFilesBundle {
    val rocc = new RoCCInterface
    val coreIdx = UInt(INPUT, width = log2Up(numCores))
    val indexOut = UInt(OUTPUT, width = log2Up(numCores))
  }
  lazy val control = new TTableControlInterface
  val regFile = new TTableRegisterFileInterface
}

class TransactionTableInterfaceLearn(implicit p: Parameters)
    extends TransactionTableInterface()(p) {
  override lazy val control = new TTableControlInterfaceLearn
}

class TransactionTableBase[StateType <: TransactionState,
  ControlReqType <: ControlReq](
  genStateVec: => Vec[StateType], genControlReq: => ControlReqType)(
  implicit p: Parameters) extends XFilesModule()(p) {
  lazy val io = new TransactionTableInterface()(p)

  // IO aliases
  lazy val cmd = new XFilesBundle {
    val readOrWrite = io.arbiter.rocc.cmd.bits.inst.funct(0)
    val isNew = io.arbiter.rocc.cmd.bits.inst.funct(1)
    val isLast = io.arbiter.rocc.cmd.bits.inst.funct(2)
    val asid = io.arbiter.rocc.cmd.bits.rs1(asidWidth + tidWidth - 1, tidWidth)
    val tid = io.arbiter.rocc.cmd.bits.rs1(tidWidth - 1, 0)
    val coreIdx = io.arbiter.coreIdx
    val countFeedback =
      io.arbiter.rocc.cmd.bits.rs1(feedbackWidth + asidWidth + tidWidth - 1,
        asidWidth + tidWidth)
    val nnid = io.arbiter.rocc.cmd.bits.rs2(nnidWidth - 1, 0)
    val data = io.arbiter.rocc.cmd.bits.rs2
    val rd = io.arbiter.rocc.cmd.bits.inst.rd
    val regId = io.arbiter.rocc.cmd.bits.rs2(63,32)
    val regValue = io.arbiter.rocc.cmd.bits.rs2(31,0)
    // Only used with learning, but maintained for assertion checking
    val transactionType = io.arbiter.rocc.cmd.bits.rs2(49,48)
    val numTrainOutputs = io.arbiter.rocc.cmd.bits.rs2(47,32) }

  // Create the actual Transaction Table
  val table = Reg(genStateVec)

  // An entry is free if it is not valid and not reserved
  def fIsFree(x: StateType): Bool = { !x.valid && !x.reserved }
  def fDerefTid(x: StateType, asid: UInt, tid: UInt): Bool = {
    (x.asid === asid) && (x.tid === tid) && (x.valid || x.reserved) }

  // Determine if there exits a free entry in the table and the index
  // of the next availble free entry
  val hasFree = table.exists(fIsFree(_))
  val nextFree = table.indexWhere(fIsFree(_))
  val foundTid = table.exists(fDerefTid(_: StateType, cmd.asid, cmd.tid))
  val derefTidIndex = table.indexWhere(fDerefTid(_: StateType, cmd.asid,
    cmd.tid))
  // io.arbiter.rocc.cmd.ready := hasFree
  io.arbiter.rocc.cmd.ready := Bool(true)
  io.arbiter.rocc.resp.valid := Bool(false)
  io.arbiter.rocc.resp.bits.rd := UInt(0)
  io.arbiter.rocc.resp.bits.data := UInt(0)
  // Default register file connections
  io.regFile.req.valid := Bool(false)
  io.regFile.req.bits.reqType := UInt(0)
  io.regFile.req.bits.tidIdx := UInt(0)
  io.regFile.req.bits.addr := UInt(0)
  io.regFile.req.bits.data := UInt(0)

  // Response pipeline to arbiter
  val arbiterRespPipe = Reg(Valid(new XFilesArbiterRespPipe))
  arbiterRespPipe.valid := Bool(false)
  arbiterRespPipe.bits.respType := UInt(0)
  arbiterRespPipe.bits.tid := UInt(0)
  arbiterRespPipe.bits.readIdx := UInt(0)
  arbiterRespPipe.bits.tidIdx := UInt(0)
  arbiterRespPipe.bits.coreIdx := UInt(0)
  arbiterRespPipe.bits.rd := UInt(0)
  arbiterRespPipe.bits.status := UInt(0)
  io.arbiter.rocc.resp.valid := arbiterRespPipe.valid

  switch (arbiterRespPipe.bits.respType) {
    is (e_TID) {
      io.arbiter.rocc.resp.bits.data := arbiterRespPipe.bits.respType ##
        UInt(0, width = 14) ##
        arbiterRespPipe.bits.tid ##
        UInt(0, width = elementWidth) }
    is (e_READ) {
      io.arbiter.rocc.resp.bits.data := arbiterRespPipe.bits.respType ##
        UInt(0, width = 14) ##
        arbiterRespPipe.bits.tid ##
        io.regFile.resp.bits.data }
    is (e_NOT_DONE) {
      io.arbiter.rocc.resp.bits.data := arbiterRespPipe.bits.respType ##
        UInt(0, width = 14) ##
        arbiterRespPipe.bits.tid ##
        arbiterRespPipe.bits.status }}
  io.arbiter.rocc.resp.bits.rd := arbiterRespPipe.bits.rd
  io.arbiter.indexOut := arbiterRespPipe.bits.coreIdx

  when (io.arbiter.rocc.cmd.valid) {
    // This is a new packet
    when (cmd.readOrWrite) { // Write == True
      when (cmd.isNew) {
        when (cmd.isLast) {
          printfInfo("X-Files saw reg write TID/Reg/Value 0x%x/0x%x/0x%x\n",
            cmd.tid, cmd.regId, cmd.regValue)
        } .otherwise {
          // [TODO] A lot of this can be removed as not everything has
          // to be initialized
          table(nextFree).reserved := Bool(true)
          table(nextFree).cacheValid := Bool(false)
          table(nextFree).waiting := Bool(false)
          table(nextFree).needsLayerInfo := Bool(true)
          table(nextFree).asid := cmd.asid
          table(nextFree).tid := cmd.tid
          table(nextFree).nnid := cmd.nnid
          table(nextFree).currentLayer := UInt(0)
          table(nextFree).countFeedback := cmd.countFeedback
          table(nextFree).done := Bool(false)
          table(nextFree).decInUse := Bool(false)
          table(nextFree).indexElement := UInt(0)
          table(nextFree).coreIdx := cmd.coreIdx
          table(nextFree).regFileAddrOut := UInt(0)
          table(derefTidIndex).nodesInCurrentLayer := UInt(0)

          arbiterRespPipe.valid := Bool(true)
          // Initiate a response that will contain the TID
          arbiterRespPipe.bits.respType := e_TID
          arbiterRespPipe.bits.tid := cmd.tid
          arbiterRespPipe.bits.tidIdx := derefTidIndex
          arbiterRespPipe.bits.coreIdx := cmd.coreIdx
          arbiterRespPipe.bits.rd := cmd.rd
          printfInfo("X-Files saw new write request for NNID 0x%x/\n",
            cmd.nnid)
        }
      }
        .elsewhen(cmd.isLast) {
        // Write data to the Register File
        io.regFile.req.valid := Bool(true)
        io.regFile.req.bits.reqType := e_TTABLE_REGFILE_WRITE
        io.regFile.req.bits.tidIdx := derefTidIndex
        io.regFile.req.bits.addr := table(derefTidIndex).indexElement
        io.regFile.req.bits.data := cmd.data
        // Update the table entry to the next block
        val nextIndexBlock = (table(derefTidIndex).indexElement(
          log2Up(regFileNumElements)-1,log2Up(elementsPerBlock)) ##
          UInt(0, width=log2Up(elementsPerBlock))) + UInt(elementsPerBlock)
        table(derefTidIndex).indexElement := nextIndexBlock
        val numInputs = table(derefTidIndex).indexElement
        val numInputsMSBs = numInputs(log2Up(regFileNumElements) - 1,
          log2Up(elementsPerBlock)) ## UInt(0, width=log2Up(elementsPerBlock))
        val numInputsLSBs = numInputs(log2Up(elementsPerBlock) - 1, 0)
        val numInputsOffset = numInputsMSBs + UInt(elementsPerBlock)
          table(derefTidIndex).valid := Bool(true)
          table(derefTidIndex).currentNode := UInt(0)
          table(derefTidIndex).readIdx := UInt(0)
          table(derefTidIndex).inFirst := Bool(true)
          table(derefTidIndex).inLast := Bool(false)
          table(derefTidIndex).needsLayerInfo := Bool(true)
          table(derefTidIndex).done := Bool(false)
          table(derefTidIndex).waiting := Bool(false)
          table(derefTidIndex).regFileLocationBit := UInt(0)
          printfInfo("TTable: LAST input write TID/data[%d] 0x%x/0x%x\n",
            table(derefTidIndex).indexElement, cmd.tid, cmd.data);
      }
        // This is an input packet
        .otherwise {
        // Write data to the Register File
        io.regFile.req.valid := Bool(true)
        io.regFile.req.bits.reqType := e_TTABLE_REGFILE_WRITE
        io.regFile.req.bits.tidIdx := derefTidIndex
        io.regFile.req.bits.addr := table(derefTidIndex).indexElement
        io.regFile.req.bits.data := cmd.data
        // Update the table entry
        table(derefTidIndex).indexElement :=
          table(derefTidIndex).indexElement + UInt(1)
        printfInfo("X-Files saw write request on TID %x with data[%d] %x\n",
          cmd.tid, table(derefTidIndex).indexElement, cmd.data);
        // table(derefTidIndex).data() :=
      }
    } .otherwise { // Ths is a read packet.
      when (table(derefTidIndex).done) {
        // Register File request
        io.regFile.req.valid := Bool(true)
        io.regFile.req.bits.addr := table(derefTidIndex).readIdx +
          table(derefTidIndex).regFileAddrOutFixed
        io.regFile.req.bits.reqType := e_TTABLE_REGFILE_READ
        // We initate the response in the arbiterRespPipe and fill in
        // data from the _guaranteed_ response from the Register File
        // on the next cycle
        arbiterRespPipe.valid := Bool(true)
        arbiterRespPipe.bits.respType := e_READ
        arbiterRespPipe.bits.tid := cmd.tid
        arbiterRespPipe.bits.tidIdx := derefTidIndex
        arbiterRespPipe.bits.readIdx := table(derefTidIndex).readIdx
        arbiterRespPipe.bits.coreIdx := cmd.coreIdx
        arbiterRespPipe.bits.rd := cmd.rd
        // Check to see if all outputs have been read and we're not in
        // some batch training state where we expect to see a new set
        // training input/output item.
        table(derefTidIndex).readIdx := table(derefTidIndex).readIdx + UInt(1)
        printfInfo("X-Files saw read request on TID %x\n",
          cmd.tid);
      } .otherwise {
        arbiterRespPipe.valid := Bool(true)
        arbiterRespPipe.bits.tid := cmd.tid
        arbiterRespPipe.bits.respType := e_NOT_DONE
        arbiterRespPipe.bits.coreIdx := cmd.coreIdx
        arbiterRespPipe.bits.rd := cmd.rd
        arbiterRespPipe.bits.status :=
          table(derefTidIndex).valid ##
          table(derefTidIndex).reserved ##
          table(derefTidIndex).cacheValid ##
          table(derefTidIndex).waiting ##
          table(derefTidIndex).needsLayerInfo ##
          table(derefTidIndex).done ##
          table(derefTidIndex).inFirst ##
          table(derefTidIndex).inLast ##
          table(derefTidIndex).neuronPointer(7,0) ##
          table(derefTidIndex).currentNodeInLayer(7,0) ##
          table(derefTidIndex).numNodes(7,0)
        printfInfo("X-Files saw read request on TID %x, but transaction not done!\n",
          cmd.tid);
      }
    }
  }

  // Update the table when we get a request from DANA
  when (io.control.resp.valid) {
    val tIdx = io.control.resp.bits.tableIndex
    // table(tIdx).waiting := Bool(true)
    when (io.control.resp.bits.cacheValid) {
      switch(io.control.resp.bits.field) {
        is(e_TTABLE_CACHE_VALID) {
          table(tIdx).cacheValid := Bool(true)
          table(tIdx).numLayers := io.control.resp.bits.data(0)
          table(tIdx).numNodes := io.control.resp.bits.data(1)
          table(tIdx).cacheIndex := io.control.resp.bits.data(2)(
            log2Up(cacheNumEntries) + errorFunctionWidth - 1, errorFunctionWidth)
          table(tIdx).decimalPoint := io.control.resp.bits.decimalPoint
          // table(tIdx).learningRate := io.control.resp.bits.data(3)
          // table(tIdx).lambda := io.control.resp.bits.data(4)
          // Once we know the cache is valid, this entry is no longer waiting
          table(tIdx).waiting := Bool(false)
          printfInfo("TTable: Updating global info from Cache...\n")
          printfInfo("  total layers:            0x%x\n",
            io.control.resp.bits.data(0))
          printfInfo("  total nodes:             0x%x\n",
            io.control.resp.bits.data(1))
          printfInfo("  cache index:             0x%x\n",
            io.control.resp.bits.data(2)(
            log2Up(cacheNumEntries) + errorFunctionWidth - 1, errorFunctionWidth))
        }
        is(e_TTABLE_LAYER) {
          table(tIdx).needsLayerInfo := Bool(false)
          table(tIdx).currentNodeInLayer := UInt(0)
          table(tIdx).nodesInCurrentLayer := io.control.resp.bits.data(0)
          table(tIdx).neuronPointer := io.control.resp.bits.data(2)

          // Once we have layer information, we can update the
          // previous and current layer addresses. These are adjusted
          // to be on block boundaries, so there's an optional round
          // term.
          val nicl = io.control.resp.bits.data(0)
          val niclMSBs = // Nodes in previous layer MSBs [TODO] fragile
            nicl(15, log2Up(elementsPerBlock)) ##
              UInt(0, width=log2Up(elementsPerBlock))
          val niclLSBs = // Nodes in previous layer LSBs
            nicl(log2Up(elementsPerBlock)-1, 0)
          val round = Mux(niclLSBs =/= UInt(0), UInt(elementsPerBlock), UInt(0))
          val niclOffset = niclMSBs + round

          val niplMSBs =
            table(tIdx).nodesInCurrentLayer(15, log2Up(elementsPerBlock)) ##
              UInt(0, width=log2Up(elementsPerBlock))
          val niplLSBs = table(tIdx).nodesInCurrentLayer(log2Up(elementsPerBlock)-1,0)
          val niplOffset = niplMSBs + Mux(niplLSBs =/= UInt(0),
            UInt(elementsPerBlock), UInt(0))

          printfInfo("TTable: Updating cache layer...\n")
          printfInfo("  total layers:               0x%x\n",
            table(tIdx).numLayers)
          printfInfo("  layer is:                   0x%x\n",
            table(tIdx).currentLayer)
          printfInfo("  neuron pointer:             0x%x\n",
            io.control.resp.bits.data(2))
          printfInfo("  nodes in current layer:     0x%x\n",
            io.control.resp.bits.data(0))
          printfInfo("  nodes in previous layer:    0x%x\n",
            io.control.resp.bits.data(1))
          printfInfo("  nicl:                       0x%x\n", nicl)
          printfInfo("  niclMSBs:                   0x%x\n", niclMSBs)
          printfInfo("  niclLSBs:                   0x%x\n", niclLSBs)
          printfInfo("  round:                      0x%x\n", round)
          printfInfo("  niplMSBs:                   0x%x\n", niplMSBs)
          printfInfo("  niplLSBs:                   0x%x\n", niplLSBs)
          printfInfo("  niplOffset:                 0x%x\n", niplOffset)
          printfInfo("  regFileAddrIn:              0x%x\n",
            table(tIdx).regFileAddrOut)
          printfInfo("  regFileAddrOut:             0x%x\n",
            table(tIdx).regFileAddrOut +  niplOffset)
        }
      }
    }
    // If the register file has all valid entries, then this specific
    // entry should stop waiting. Note, that this logic will correctly
    // overwrite that of the e_TTABLE_LAYER.
    when (io.control.resp.bits.layerValid) {
      val tIdx = io.control.resp.bits.layerValidIndex
      val inLastOld = table(tIdx).inLast
      val inLastNew = table(tIdx).currentLayer === (table(tIdx).numLayers - UInt(1))
      table(tIdx).inLast := inLastNew
      printfInfo("TTable: RegFile has all outputs of tIdx 0x%x\n",
        io.control.resp.bits.layerValidIndex)
    }
  }

  val readyCache = Reg(next = io.control.resp.bits.readyCache)
  val readyPeTable = Reg(next = io.control.resp.bits.readyPeTable)
  // Round Robin Arbitration of Transaction Table entries. One of
  // these is passed out over an interface to DANA's control module.
  val entryArbiter = Module(new RRArbiter(genControlReq,
    transactionTableNumEntries))
  for (i <- 0 until transactionTableNumEntries) {
    val isValid = table(i).valid
    val isNotWaiting = !table(i).waiting
    val noRequestLastCycle = Reg(next = !entryArbiter.io.out.valid)
    val cacheWorkToDo = (table(i).decInUse || !table(i).cacheValid ||
      table(i).needsLayerInfo)
    val peWorkToDo = (table(i).currentNode =/= table(i).numNodes)
    // The entryArbiter has a valid request if that TTable entry is
    // valid, it is not waiting, a request was not generated last
    // cycle, and either there is cache or PE table work to do and the
    // backend can support one of these.
    entryArbiter.io.in(i).valid := isValid && isNotWaiting &&
      noRequestLastCycle &&
      ((readyCache && cacheWorkToDo) || (readyPeTable && peWorkToDo))
    // The other data connections are just aliases to the contents of
    // the specific table entry
    entryArbiter.io.in(i).bits := table(i)
    entryArbiter.io.in(i).bits.isDone := table(i).decInUse
    entryArbiter.io.in(i).bits.tableIndex := UInt(i)
  }
  io.control.req <> entryArbiter.io.out

  // Do a special table update if the arbiter is allowing a request to
  // go through. This decreases the necessary bandwidth between the
  // Control module and the Transaction Table as the Control module
  // doesn't have to generate responses. [TODO] This is somewhat kludgy
  // and may result in excess combinational logic depth. It may be
  // necessary to pipeline this.
  val isCacheReq = entryArbiter.io.out.valid &&
    (!entryArbiter.io.out.bits.cacheValid ||
      entryArbiter.io.out.bits.needsLayerInfo ||
      entryArbiter.io.out.bits.isDone)
  val isPeReq = entryArbiter.io.out.valid &&
    (entryArbiter.io.out.bits.cacheValid &&
      !entryArbiter.io.out.bits.needsLayerInfo) &&
    !entryArbiter.io.out.bits.isDone
  // If this is a transition into a layer which is not the first
  // layer, then the Transaction Table requests need to block
  // until the Register File has all valid data. [TODO] This is
  // a sub-optimal design choice as PEs should be allowed to
  // start before the Register File has _all_ of its valid data,
  // but I'm leaving this the way it is due to the lack of a
  // non-trivial path to add this functionality.
  when (isCacheReq) {
    val tIdx = entryArbiter.io.out.bits.tableIndex
    table(tIdx).waiting := Bool(true)
    when (entryArbiter.io.out.bits.isDone) {
      printfInfo("TTable entry for ASID/TID %x/%x is done\n",
        table(tIdx).asid, table(tIdx).tid);
        table(tIdx).done := Bool(true) }}
  when (isPeReq) {
    val tIdx = entryArbiter.io.out.bits.tableIndex
    val inLastNode = table(tIdx).currentNodeInLayer ===
      table(tIdx).nodesInCurrentLayer - UInt(1)
    val notInLastLayer = table(tIdx).currentLayer <
      (table(tIdx).numLayers - UInt(1))
    table(tIdx).currentNode := table(tIdx).currentNode + UInt(1)
    table(tIdx).currentNodeInLayer := table(tIdx).currentNodeInLayer + UInt(1) }

  // Dump table information
  when (isPeReq || io.control.resp.valid) {
    info(table, "ttable,") }

  // Reset Condition
  when (reset) {for (i <- 0 until transactionTableNumEntries) {
    table(i).valid := Bool(false)
    table(i).reserved := Bool(false) }}

  // Assertions

  // The X-FILES arbiter should only receive a new transaction request
  // if it is asserting its ready signal (it has a free entry)
  assert(!(io.arbiter.rocc.cmd.valid && cmd.readOrWrite && cmd.isNew &&
    !cmd.isLast && !hasFree),
    "TTable saw new write req, but doesn't have any free entries")

  // Only one inbound request or response on the same line can
  // currently be handled. Due to the split nature of cache and
  // register file responses, both have to be checked.
  assert(!(io.arbiter.rocc.cmd.valid && cmd.readOrWrite && cmd.isNew &&
    io.control.resp.bits.cacheValid &&
    (io.control.resp.bits.tableIndex === nextFree)),
    "TTable saw new write req on same entry as control resp from Cache")
  assert(!(io.arbiter.rocc.cmd.valid && cmd.readOrWrite && cmd.isNew &&
    io.control.resp.bits.layerValid &&
    (io.control.resp.bits.layerValidIndex === nextFree)),
    "TTable saw new write req on same entry as control resp from Reg File")

  assert(!(io.arbiter.rocc.cmd.valid && cmd.readOrWrite && !cmd.isNew &&
    io.control.resp.bits.cacheValid &&
    (io.control.resp.bits.tableIndex === derefTidIndex)),
    "TTable saw non-new write req on same entry as control resp from Cache")
  assert(!(io.arbiter.rocc.cmd.valid && cmd.readOrWrite && !cmd.isNew &&
    io.control.resp.bits.layerValid &&
    (io.control.resp.bits.layerValidIndex === derefTidIndex)),
    "TTable saw non-new write req on same entry as control resp from Reg File")

  // Valid should never be true if reserved is not true
  for (i <- 0 until transactionTableNumEntries)
    assert(!table(i).valid || table(i).reserved,
      "Valid asserted with reserved de-asserted on TTable " + i)

  // A read request or a non-new write request should hit a valid
  // entry
  assert(!(!foundTid && io.arbiter.rocc.cmd.valid &&
    (!cmd.readOrWrite || (cmd.readOrWrite && !cmd.isNew))),
    "TTable saw read or non-new write req on a non-existent ASID/TID")
  // A new write request should not hit a tid
  assert(!(foundTid && io.arbiter.rocc.cmd.valid &&
    cmd.readOrWrite && cmd.isNew && !cmd.isLast),
    "TTable saw new write req on an existing ASID/TID")
  // A register write should hit a tid
  assert(!(!foundTid && io.arbiter.rocc.cmd.valid &&
    cmd.readOrWrite && cmd.isNew && cmd.isLast),
    "TTable saw write register on non-existent ASID/TID")

  // A Control response should never have a cacheValid or layerValid
  // asserted when the decoupled valid is deasserted
  assert(!(!io.control.resp.valid &&
    (io.control.resp.bits.cacheValid || io.control.resp.bits.layerValid)),
    "TTable control response deasserted, but cacheValid or layerValid asserted")

  // The current node should never be greater than the total number of nodes
  assert(!Vec((0 until transactionTableNumEntries).map(i =>
    table(i).valid && (table(i).currentNode > table(i).numNodes))).contains(Bool(true)),
    "A TTable entry has a currentNode count greater than the total numNodes")

  // Don't send a response to the core unless it's ready
  assert(!(io.arbiter.rocc.resp.valid && !io.arbiter.rocc.resp.ready),
    "TTable tried to send a valid response when core was not ready")

  // Check to make sure that the Register File is producing the
  // expected timing
  assert(!(io.arbiter.rocc.resp.valid &&
    arbiterRespPipe.bits.respType === e_READ &&
    !io.regFile.resp.valid),
    "TTable sending valid RoCC read response, but RegisterFile response not valid")


  // No writes should show up if the transaction is already valid
  assert(!(io.arbiter.rocc.cmd.valid && cmd.readOrWrite &&
    table(derefTidIndex).valid),
    "TTable saw write requests on valid TID")

  // If learning is disabled we should never see a learning request
  if (!learningEnabled) {
    assert(!(io.arbiter.rocc.cmd.valid &&
      (cmd.transactionType === e_TTYPE_INCREMENTAL ||
        cmd.transactionType === e_TTYPE_BATCH)),
      "Built X-Files does not support learning") }
}

class TransactionTable(implicit p: Parameters)
    extends TransactionTableBase[TransactionState, ControlReq](
  Vec(p(TransactionTableNumEntries), new TransactionState),
      new ControlReq)(p) {
  when (io.arbiter.rocc.cmd.valid) {
    when (cmd.readOrWrite) {
      when (cmd.isNew) {
        when (cmd.isLast) {
        } .otherwise {
        }
      } .elsewhen(cmd.isLast) {
        val nextIndexBlock = (table(derefTidIndex).indexElement(
          log2Up(regFileNumElements)-1,log2Up(elementsPerBlock)) ##
          UInt(0, width=log2Up(elementsPerBlock))) + UInt(elementsPerBlock)
        val numInputs = table(derefTidIndex).indexElement
        val numInputsMSBs = numInputs(log2Up(regFileNumElements) - 1,
          log2Up(elementsPerBlock)) ## UInt(0, width=log2Up(elementsPerBlock))
        val numInputsLSBs = numInputs(log2Up(elementsPerBlock) - 1, 0)
        val numInputsOffset = numInputsMSBs + UInt(elementsPerBlock)
        table(derefTidIndex).nodesInCurrentLayer := numInputsOffset
      } .otherwise {
      }
    } .otherwise {
      when (table(derefTidIndex).done) {
        when ((table(derefTidIndex).readIdx ===
          table(derefTidIndex).nodesInCurrentLayer - UInt(1))) {
          table(derefTidIndex).valid := Bool(false)
          table(derefTidIndex).reserved := Bool(false)
          printfInfo("TTable: All outputs read, evicting ASID/TID 0x%x/0x%x\n",
            table(derefTidIndex).asid, table(derefTidIndex).tid)
        }
      } .otherwise {
      }
    }
  }

  when (io.control.resp.valid) {
    val tIdx = io.control.resp.bits.tableIndex
    when (io.control.resp.bits.cacheValid) {
      switch (io.control.resp.bits.field) {
        is (e_TTABLE_LAYER) {
          val nicl = io.control.resp.bits.data(0)
          val niclMSBs = // Nodes in previous layer MSBs [TODO] fragile
            nicl(15, log2Up(elementsPerBlock)) ##
          UInt(0, width=log2Up(elementsPerBlock))
          val niclLSBs = // Nodes in previous layer LSBs
            nicl(log2Up(elementsPerBlock)-1, 0)
          val round = Mux(niclLSBs =/= UInt(0), UInt(elementsPerBlock), UInt(0))
          val niclOffset = niclMSBs + round

          val niplMSBs =
            table(tIdx).nodesInCurrentLayer(15, log2Up(elementsPerBlock)) ##
          UInt(0, width=log2Up(elementsPerBlock))
          val niplLSBs = table(tIdx).nodesInCurrentLayer(log2Up(elementsPerBlock)-1,0)
          val niplOffset = niplMSBs + Mux(niplLSBs =/= UInt(0),
            UInt(elementsPerBlock), UInt(0))
          table(tIdx).waiting := Bool(false)
          table(tIdx).regFileAddrIn := table(tIdx).regFileAddrOut
          table(tIdx).regFileAddrOut := table(tIdx).regFileAddrOut + niplOffset
          table(tIdx).regFileAddrOutFixed := table(tIdx).regFileAddrOut +
            niplOffset
          printfInfo("  inFirst/inLast: 0x%x/0x%x\n", table(tIdx).inFirst,
            table(tIdx).inLast)
        }
      }
    }
    when (io.control.resp.bits.layerValid) {
      val tIdx = io.control.resp.bits.layerValidIndex
      val inLastOld = table(tIdx).inLast
      val inLastNew = table(tIdx).currentLayer === (table(tIdx).numLayers - UInt(1))
      when (!inLastOld) {
        table(tIdx).waiting := Bool(false)
      } .otherwise {
        table(tIdx).decInUse := Bool(true)
        table(tIdx).waiting := Bool(false)
      }
      printfInfo("  inFirst/inLast: 0x%x/0x%x->0x%x\n",
        table(tIdx).inFirst, inLastOld, inLastNew)
    }
  }

  when (isPeReq) {
    val tIdx = entryArbiter.io.out.bits.tableIndex
    val inLastNode = table(tIdx).currentNodeInLayer ===
      table(tIdx).nodesInCurrentLayer - UInt(1)
    val notInLastLayer = table(tIdx).currentLayer <
      (table(tIdx).numLayers - UInt(1))
    when(inLastNode && notInLastLayer) {
      table(tIdx).needsLayerInfo := Bool(true)
      table(tIdx).currentLayer := table(tIdx).currentLayer + UInt(1)
      table(tIdx).regFileLocationBit := !table(tIdx).regFileLocationBit
    } .otherwise {
      table(tIdx).needsLayerInfo := Bool(false)
      table(tIdx).currentLayer := table(tIdx).currentLayer
    }
    table(tIdx).inFirst := table(tIdx).currentLayer === UInt(0)
  }

}

class TransactionTableLearn(implicit p: Parameters)
    extends TransactionTableBase[TransactionStateLearn, ControlReqLearn](
  Vec(p(TransactionTableNumEntries), new TransactionStateLearn),
    new ControlReqLearn)(p) {
  override lazy val io = new TransactionTableInterfaceLearn()(p)

  when (io.arbiter.rocc.cmd.valid) {
    when (cmd.readOrWrite) {
      when (cmd.isNew) {
        when (cmd.isLast) {
          // This is a register write
          switch(cmd.regId) {
            is (e_TTABLE_WRITE_REG_BATCH_ITEMS) {
              table(derefTidIndex).numBatchItems := cmd.regValue
              printfInfo("TTable setting TID 0x%x numBatchItems to 0x%x\n",
                cmd.tid, cmd.regValue)}
            is (e_TTABLE_WRITE_REG_LEARNING_RATE) {
              table(derefTidIndex).learningRate := cmd.regValue
              printfInfo("TTable setting TID 0x%x learningRate to 0x%x\n",
                cmd.tid, cmd.regValue)}
            is (e_TTABLE_WRITE_REG_WEIGHT_DECAY_LAMBDA) {
              table(derefTidIndex).lambda := cmd.regValue
              printfInfo("TTable setting TID 0x%x lambda to 0x%x\n",
                cmd.tid, cmd.regValue)}
          }
        } .otherwise {
          table(nextFree).transactionType := cmd.transactionType
          when (cmd.transactionType === e_TTYPE_INCREMENTAL ||
            cmd.transactionType === e_TTYPE_BATCH) {
            table(nextFree).numTrainOutputs := cmd.numTrainOutputs
            table(nextFree).stateLearn := e_TTABLE_STATE_LOAD_OUTPUTS
          } .otherwise {
            table(nextFree).stateLearn := e_TTABLE_STATE_FEEDFORWARD
          }
          table(nextFree).regFileAddrInFixed := UInt(0)
          // [TODO] Temporary value for number of batch items
          table(nextFree).numBatchItems := UInt(1)
          table(nextFree).curBatchItem := UInt(0)
          table(nextFree).transactionType := cmd.transactionType
          printfInfo("X-Files saw new write request for NNID/TType 0x%x/0x%x\n",
            cmd.nnid, cmd.transactionType)
        }
      } .elsewhen(cmd.isLast) {
        val nextIndexBlock = (table(derefTidIndex).indexElement(
          log2Up(regFileNumElements)-1,log2Up(elementsPerBlock)) ##
          UInt(0, width=log2Up(elementsPerBlock))) + UInt(elementsPerBlock)
        val numInputs = table(derefTidIndex).indexElement
        val numInputsMSBs = numInputs(log2Up(regFileNumElements) - 1,
          log2Up(elementsPerBlock)) ## UInt(0, width=log2Up(elementsPerBlock))
        val numInputsLSBs = numInputs(log2Up(elementsPerBlock) - 1, 0)
        val numInputsOffset = numInputsMSBs + UInt(elementsPerBlock)
        when (table(derefTidIndex).stateLearn === e_TTABLE_STATE_LOAD_OUTPUTS) {
          table(derefTidIndex).stateLearn := e_TTABLE_STATE_LEARN_FEEDFORWARD
          table(derefTidIndex).regFileAddrInFixed := nextIndexBlock
          table(derefTidIndex).regFileAddrOut := nextIndexBlock
          printfInfo("TTable: LAST E[output] write TID/data 0x%x/0x%x\n",
            cmd.tid, cmd.data);
          table(derefTidIndex).valid := Bool(false)
        } .otherwise {
          table(derefTidIndex).nodesInCurrentLayer :=
            numInputsOffset - table(derefTidIndex).regFileAddrInFixed
          printfInfo("TTable: Setting nodesInCurrentLayer to 0x%x\n",
            numInputsOffset - table(derefTidIndex).regFileAddrInFixed)
        }
        table(derefTidIndex).inLastEarly := Bool(false)
        table(derefTidIndex).regFileAddrIn := UInt(0)
        // table(derefTidIndex).regFileAddrDelta := UInt(0)
        table(derefTidIndex).regFileAddrDW := UInt(0)
        table(derefTidIndex).regFileAddrSlope := UInt(0)
        table(derefTidIndex).offsetBias := UInt(0)
        table(derefTidIndex).offsetDW := UInt(0)
      } .otherwise {
      }
    } .otherwise {
      when (table(derefTidIndex).done) {
        // [TODO] #54: this comparison for an exit condition is
        // causing problems with incremental learning. I'm not
        // planning to read in the output layer, so the exit condition
        // for looking that the readIdx is equal to the number of
        // nodes in the current layer is valid.
        printfInfo("nodesInLast: 0x%x\n", table(derefTidIndex).nodesInLast)
        when ((table(derefTidIndex).readIdx ===
          table(derefTidIndex).nodesInLast - UInt(1)) &&
          (table(derefTidIndex).stateLearn =/= e_TTABLE_STATE_LOAD_OUTPUTS)) {
          table(derefTidIndex).valid := Bool(false)
          table(derefTidIndex).reserved := Bool(false)
          printfInfo("TTable: All outputs read, evicting ASID/TID 0x%x/0x%x\n",
            table(derefTidIndex).asid, table(derefTidIndex).tid)
          printfInfo("        State is: 0x%x\n",
            table(derefTidIndex).stateLearn)
        }
      } .otherwise {
      }
    }
  }

  // Update the table when we get a request from DANA
  when (io.control.resp.valid) {
    val tIdx = io.control.resp.bits.tableIndex
    // table(tIdx).waiting := Bool(true)
    when (io.control.resp.bits.cacheValid) {
      switch(io.control.resp.bits.field) {
        is(e_TTABLE_CACHE_VALID) {
          table(tIdx).globalWtptr := io.control.resp.bits.globalWtptr
          when (table(tIdx).transactionType === e_TTYPE_BATCH) {
            table(tIdx).numNodes := io.control.resp.bits.data(1) * UInt(2)
          }
          table(tIdx).errorFunction := io.control.resp.bits.data(2)(
            errorFunctionWidth - 1, 0)
          table(tIdx).numWeightBlocks := io.control.resp.bits.data(5)
          printfInfo("  error function:          0x%x\n",
            io.control.resp.bits.data(2)(
              errorFunctionWidth - 1, 0))
          printfInfo("  learning rate:           0x%x (NOT SET)\n",
            io.control.resp.bits.data(3))
          printfInfo("  lambda:                  0x%x (NOT SET)\n",
            io.control.resp.bits.data(4))
          printfInfo("  Totalweightblocks :      0x%x\n",
            io.control.resp.bits.data(5))
          printfInfo("  Global Weight Pointer :  0x%x\n",
            io.control.resp.bits.globalWtptr)
        }
        is(e_TTABLE_LAYER) {
          val nicl = io.control.resp.bits.data(0)
          val niclMSBs = // Nodes in previous layer MSBs [TODO] fragile
            nicl(15, log2Up(elementsPerBlock)) ##
              UInt(0, width=log2Up(elementsPerBlock))
          val niclLSBs = // Nodes in previous layer LSBs
            nicl(log2Up(elementsPerBlock)-1, 0)
          val round = Mux(niclLSBs =/= UInt(0), UInt(elementsPerBlock), UInt(0))
          val niclOffset = niclMSBs + round

          val nipl = io.control.resp.bits.data(1)
          val niplMSBs = nipl(15, log2Up(elementsPerBlock)) ##
              UInt(0, width=log2Up(elementsPerBlock))
          val niplLSBs = nipl(log2Up(elementsPerBlock)-1,0)
          val niplOffset = niplMSBs + Mux(niplLSBs =/= UInt(0),
            UInt(elementsPerBlock), UInt(0))

          printfInfo("  nicl:             0x%x\n", nicl)
          printfInfo("  niclOffset:       0x%x\n", niclOffset)
          printfInfo("  nipl:             0x%x\n", nipl)
          printfInfo("  niplOffset:       0x%x\n", niplOffset)
          when ((table(tIdx).currentLayer === UInt(0)) &&
            (table(tIdx).stateLearn === e_TTABLE_STATE_LEARN_FEEDFORWARD ||
              table(tIdx).stateLearn === e_TTABLE_STATE_FEEDFORWARD)) {
            table(tIdx).waiting := Bool(false)
          }
          switch(table(tIdx).stateLearn) {
            is(e_TTABLE_STATE_FEEDFORWARD){
              table(tIdx).regFileAddrIn := table(tIdx).regFileAddrOut
              table(tIdx).regFileAddrOut := table(tIdx).regFileAddrOut +
                niplOffset
              table(tIdx).regFileAddrOutFixed :=
                table(tIdx).regFileAddrOut + niplOffset
              // nodesInLast can be blindly set during non-learning
              // feedforward mode
              table(tIdx).nodesInLast := nicl
            }
            is(e_TTABLE_STATE_LEARN_FEEDFORWARD){
              val regFileAddrOut = table(tIdx).regFileAddrOut + niplOffset
              table(tIdx).regFileAddrIn := table(tIdx).regFileAddrOut
              table(tIdx).regFileAddrOut := regFileAddrOut
              table(tIdx).regFileAddrOutFixed := regFileAddrOut
              table(tIdx).regFileAddrDW := regFileAddrOut + niclOffset

              // Update the number of total nodes in the network
              when (table(tIdx).currentLayer === UInt(0)) { // In first layer
                table(tIdx).numNodes := table(tIdx).numNodes + nicl
              } .elsewhen (table(tIdx).inLastEarly) {        // in the last layer
                table(tIdx).numNodes := table(tIdx).numNodes + nicl
              } .otherwise {                                // not first or last
                table(tIdx).numNodes := table(tIdx).numNodes + nicl * UInt(2)
              }

              // The bias offset is the size of the bias region
              val offsetBias = table(tIdx).offsetBias + niclOffset
              table(tIdx).offsetBias := offsetBias
              val biasAddr = regFileAddrOut + table(tIdx).offsetBias +
                table(tIdx).offsetDW + niclOffset
              table(tIdx).biasAddr := biasAddr
              table(tIdx).regFileAddrSlope := biasAddr + niclOffset
              printfInfo("  offsetBias:       0x%x\n",
                table(tIdx).offsetBias + niclOffset)
              // The DW offset is the size of the DW region
              when (!table(tIdx).inLastEarly) {
                table(tIdx).offsetDW := table(tIdx).offsetDW + niclOffset
                printfInfo("  offsetDW:         0x%x\n",
                  table(tIdx).offsetDW + niclOffset)
              }

              // Store the number of nodes in the output layer for future use
              when (table(tIdx).inLastEarly) {
                table(tIdx).nodesInLast := nicl
              }

              printfInfo("  offsetDW:         0x%x\n", table(tIdx).offsetDW)
              printfInfo("  regFileAddrDw:    0x%x -> 0x%x\n",
                table(tIdx).regFileAddrDW, regFileAddrOut + niclOffset)
              printfInfo("  regFileAddrSlope: 0x%x\n", biasAddr + niclOffset)
              printfInfo("  biasAddr:         0x%x\n", biasAddr)
            }
            is(e_TTABLE_STATE_LEARN_ERROR_BACKPROP){
              table(tIdx).regFileAddrOut := table(tIdx).regFileAddrDW
              table(tIdx).regFileAddrDW := table(tIdx).regFileAddrDW +
                niclOffset

              // Handle special case of being in the last hidden
              // layer. Also, setup the Auxiliary address which, in
              // this state, is used to store the address of the
              // previous layer's inputs
              when (table(tIdx).currentLayer === table(tIdx).numLayers - UInt(2)) {
                val regFileAddrIn = table(tIdx).regFileAddrIn
                val regFileAddrAux = regFileAddrIn - niplOffset
                //address to read outputs to compute derivative
                table(tIdx).regFileAddrIn := regFileAddrIn
                table(tIdx).regFileAddrAux := regFileAddrAux
                printfInfo("  regFileAddrIn:    0x%x\n", regFileAddrIn)
                printfInfo("  regFileAddrAux:   0x%x\n", regFileAddrAux)
              } .otherwise {
                val regFileAddrIn = table(tIdx).regFileAddrIn - niclOffset
                val regFileAddrAux = regFileAddrIn - niplOffset
                table(tIdx).regFileAddrIn := regFileAddrIn
                table(tIdx).regFileAddrAux := regFileAddrAux
                printfInfo("  regFileAddrIn:    0x%x\n", regFileAddrIn)
                printfInfo("  regFileAddrAux:   0x%x\n", regFileAddrAux)
              }

              // [TODO] Check that this is working
              table(tIdx).biasAddr := table(tIdx).biasAddr - niclOffset
              printfInfo("  offsetBias:       0x%x\n", table(tIdx).offsetBias)
              printfInfo("  offsetDW:         0x%x\n", table(tIdx).offsetDW)
              printfInfo("  regFileAddrDw:    0x%x -> 0x%x\n",
                table(tIdx).regFileAddrDW, table(tIdx).regFileAddrDW + niclOffset)
              printfInfo("  regFileAddrSlope: 0x%x\n",
                table(tIdx).regFileAddrSlope)
              printfInfo("  biasAddr:         0x%x\n",
                table(tIdx).regFileAddrDW - niclOffset)

            }
            is(e_TTABLE_STATE_LEARN_WEIGHT_UPDATE){
              when(table(tIdx).transactionType === e_TTYPE_BATCH){
                printfInfo("TTable Layer Update, state == LEARN_WEIGHT_UPDATE\n")
                when (table(tIdx).currentLayer === UInt(0)){
                  table(tIdx).regFileAddrDW := table(tIdx).regFileAddrInFixed
                  table(tIdx).regFileAddrIn := table(tIdx).regFileAddrInFixed +
                    niclOffset

                  // If we're in the first layer, then we need to go
                  // ahead and update the slope address. We can
                  // compute this because we know both the slope
                  // offset (which is the offset from the bias region
                  // to the start of the weight update region).
                  table(tIdx).biasAddr := table(tIdx).regFileAddrSlope -
                    table(tIdx).offsetBias
                  printfInfo("  regFileAddrDw:   0x%x\n",
                    table(tIdx).regFileAddrInFixed)
                  printfInfo("  regFileAddrIn:   0x%x\n",
                    table(tIdx).regFileAddrInFixed + niclOffset)
                }.otherwise{
                  table(tIdx).regFileAddrDW := table(tIdx).regFileAddrIn
                  table(tIdx).regFileAddrIn := table(tIdx).regFileAddrIn + niclOffset
                  table(tIdx).biasAddr := table(tIdx).biasAddr + niplOffset
                  printfInfo("  regFileAddrDw:   0x%x\n",
                    table(tIdx).regFileAddrIn)
                  printfInfo("  regFileAddrIn:   0x%x\n",
                    table(tIdx).regFileAddrInFixed + niclOffset)
                  printfInfo("  biasAddr:        0x%x\n",
                    table(tIdx).biasAddr + niplOffset)
                }
              }.otherwise{
                table(tIdx).regFileAddrDW := table(tIdx).regFileAddrIn
                table(tIdx).regFileAddrIn := table(tIdx).regFileAddrIn + niplOffset
              }
            }
          }
          printfInfo("  inFirst/inLast/inLastEarly: 0x%x/0x%x/0x%x\n",
            table(tIdx).inFirst, table(tIdx).inLast, table(tIdx).inLastEarly)
        }
      }
    }
    when (io.control.resp.bits.layerValid) {
      val tIdx = io.control.resp.bits.layerValidIndex
      val inLastOld = table(tIdx).inLast
      val inLastNew = table(tIdx).currentLayer === (table(tIdx).numLayers - UInt(1))
      val inFirst = table(tIdx).currentLayer === UInt(0)
      switch (table(tIdx).transactionType) {
        is (e_TTYPE_FEEDFORWARD) {
          when (!inLastOld) {
            table(tIdx).waiting := Bool(false)
          } .otherwise {
            table(tIdx).decInUse := Bool(true)
            table(tIdx).waiting := Bool(false)
          }
        }
        is (e_TTYPE_INCREMENTAL) {
          when (inFirst &&
            table(tIdx).stateLearn === e_TTABLE_STATE_LEARN_WEIGHT_UPDATE) {
            table(tIdx).decInUse := Bool(true)
            table(tIdx).waiting := Bool(false)
          } .otherwise {
            table(tIdx).waiting := Bool(false)
          }
        }
        is (e_TTYPE_BATCH) {
          when (inLastOld &&
            table(tIdx).currentLayer === (table(tIdx).numLayers - UInt(1)) &&
            table(tIdx).stateLearn === e_TTABLE_STATE_LEARN_WEIGHT_UPDATE) {
            table(tIdx).waiting := Bool(false)
            table(tIdx).decInUse := Bool(true)
          } .elsewhen (table(tIdx).stateLearn === e_TTABLE_STATE_LOAD_OUTPUTS) {
            table(tIdx).indexElement := UInt(0)
            table(tIdx).done := Bool(true)
            table(tIdx).valid := Bool(false)
          } .otherwise {
            table(tIdx).waiting := Bool(false)
          }
        }
      }
      printfInfo("  inFirst/inLast/inLastEarly/state: 0x%x/0x%x->0x%x/0x%x/0x%x\n",
        table(tIdx).inFirst, inLastOld, inLastNew, table(tIdx).inLastEarly,
        table(tIdx).stateLearn)
    }
  }

  for (i <- 0 until transactionTableNumEntries) {
    entryArbiter.io.in(i).bits.globalWtptr := table(i).globalWtptr
    entryArbiter.io.in(i).bits.transactionType := table(i).transactionType
    entryArbiter.io.in(i).bits.batchFirst := table(i).curBatchItem === UInt(0)
    entryArbiter.io.in(i).bits.errorFunction := table(i).errorFunction
    entryArbiter.io.in(i).bits.learningRate := table(i).learningRate
    entryArbiter.io.in(i).bits.lambda := table(i).lambda
    entryArbiter.io.in(i).bits.numWeightBlocks := table(i).numWeightBlocks
    entryArbiter.io.in(i).bits.regFileAddrDW := table(i).regFileAddrDW
    entryArbiter.io.in(i).bits.regFileAddrSlope := table(i).regFileAddrSlope
    entryArbiter.io.in(i).bits.regFileAddrBias := table(i).biasAddr
    entryArbiter.io.in(i).bits.regFileAddrAux := table(i).regFileAddrAux
    entryArbiter.io.in(i).bits.stateLearn := table(i).stateLearn
    entryArbiter.io.in(i).bits.inLastEarly := table(i).inLastEarly
  }

  when (isPeReq) {
    val tIdx = entryArbiter.io.out.bits.tableIndex
    val inLastNode = table(tIdx).currentNodeInLayer ===
      table(tIdx).nodesInCurrentLayer - UInt(1)
    val notInLastLayer = table(tIdx).currentLayer <
      (table(tIdx).numLayers - UInt(1))

    // If we're at the end of a layer, we need new layer information
    // The comparison here differs from how this is handled in
    // nn_instruction.v.
    switch(table(tIdx).stateLearn){
      is(e_TTABLE_STATE_FEEDFORWARD){
        when(inLastNode && notInLastLayer) {
          table(tIdx).needsLayerInfo := Bool(true)
          table(tIdx).currentLayer := table(tIdx).currentLayer + UInt(1)
          table(tIdx).regFileLocationBit := !table(tIdx).regFileLocationBit
        } .otherwise {
          table(tIdx).needsLayerInfo := Bool(false)
          table(tIdx).currentLayer := table(tIdx).currentLayer
        }
        table(tIdx).inFirst := table(tIdx).currentLayer === UInt(0)
      }
      is(e_TTABLE_STATE_LEARN_FEEDFORWARD){
        when(table(tIdx).inLast && inLastNode){
          table(tIdx).needsLayerInfo := Bool(true)
          table(tIdx).currentLayer := table(tIdx).currentLayer - UInt(1)
          table(tIdx).regFileLocationBit := !table(tIdx).regFileLocationBit
          table(tIdx).inFirst := table(tIdx).currentLayer === UInt(1)
          table(tIdx).inLastEarly := Bool(true)
          table(tIdx).stateLearn := e_TTABLE_STATE_LEARN_ERROR_BACKPROP
        } .elsewhen (inLastNode && notInLastLayer) {
          table(tIdx).needsLayerInfo := Bool(true)
          table(tIdx).currentLayer := table(tIdx).currentLayer + UInt(1)
          table(tIdx).regFileLocationBit := !table(tIdx).regFileLocationBit
          table(tIdx).inFirst := table(tIdx).currentLayer === UInt(0)

          // inLastEarly will assert as soon as the last PE Request goes
          // out. This is useful if you need something that goes high at
          // the earliest possible definition of "being in the last
          // layer", e.g., when generating a request for the next layer
          // information.
          table(tIdx).inLastEarly :=
            table(tIdx).currentLayer === (table(tIdx).numLayers - UInt(2))
        } .otherwise {
          table(tIdx).needsLayerInfo := Bool(false)
          table(tIdx).currentLayer := table(tIdx).currentLayer
          table(tIdx).inFirst := table(tIdx).currentLayer === UInt(0)
        }
      }
      is(e_TTABLE_STATE_LEARN_ERROR_BACKPROP){
        when(table(tIdx).inFirst && inLastNode) {
          // table(tIdx).needsLayerInfo := Bool(true)
          table(tIdx).needsLayerInfo := Bool(false)
          // table(tIdx).currentLayer := table(tIdx).currentLayer + UInt(1)
          table(tIdx).currentLayer := UInt(0)
          table(tIdx).regFileLocationBit := !table(tIdx).regFileLocationBit
          when(table(tIdx).transactionType === e_TTYPE_BATCH){
            when (table(tIdx).curBatchItem === (table(tIdx).numBatchItems - UInt(1))) {
              table(tIdx).needsLayerInfo := Bool(true)
              table(tIdx).stateLearn := e_TTABLE_STATE_LEARN_WEIGHT_UPDATE
            } .otherwise {
              table(tIdx).stateLearn := e_TTABLE_STATE_LOAD_OUTPUTS
              table(tIdx).curBatchItem := table(tIdx).curBatchItem + UInt(1)
            }
          }.otherwise{
            // [TODO] Related to a fix for #54
            table(tIdx).stateLearn := e_TTABLE_STATE_LEARN_WEIGHT_UPDATE
          }
          table(tIdx).inFirst := Bool(false)
          table(tIdx).inLastEarly :=
            table(tIdx).currentLayer === (table(tIdx).numLayers - UInt(2))
        } .elsewhen(inLastNode && (table(tIdx).currentLayer > UInt(0))) {
          table(tIdx).needsLayerInfo := Bool(true)
          table(tIdx).currentLayer := table(tIdx).currentLayer - UInt(1)
          table(tIdx).regFileLocationBit := !table(tIdx).regFileLocationBit
          table(tIdx).inFirst := table(tIdx).currentLayer === UInt(1)
        } .otherwise {
          table(tIdx).needsLayerInfo := Bool(false)
          table(tIdx).currentLayer := table(tIdx).currentLayer
        }
      }
      is (e_TTABLE_STATE_LEARN_WEIGHT_UPDATE) {
        // when(table(tIdx).transactionType === e_TTYPE_INCREMENTAL){
        when(table(tIdx).inLast && inLastNode){
            table(tIdx).waiting := Bool(true)
          table(tIdx).currentLayer := table(tIdx).currentLayer
        } .elsewhen(inLastNode && notInLastLayer) {
          table(tIdx).needsLayerInfo := Bool(true)
          table(tIdx).currentLayer := table(tIdx).currentLayer + UInt(1)
          table(tIdx).regFileLocationBit := !table(tIdx).regFileLocationBit
        } .otherwise {
          table(tIdx).needsLayerInfo := Bool(false)
          table(tIdx).currentLayer := table(tIdx).currentLayer
          table(tIdx).inLastEarly :=
          table(tIdx).currentLayer === (table(tIdx).numLayers - UInt(2))
        }
      }
    }
  }

  // Catch any jumps to an error state
  (0 until transactionTableNumEntries).map(i =>
    assert(!((table(i).valid || table(i).reserved) &&
      table(i).stateLearn === e_TTABLE_STATE_ERROR),
      "TTable Transaction is in error state"))

}

class TransactionTableTests(uut: TransactionTable, isTrace: Boolean = true)
    extends DanaTester(uut, isTrace) {
  for (t <- 0 until 3) {
    peek(uut.hasFree)
    peek(uut.nextFree)
    val tid = t
    val nnid = t + 15 * 16
    // newWriteRequest(uut.io.arbiter.rocc, tid, nnid)
    // writeRndData(uut.io.arbiter.rocc, tid, nnid, 5, 10)
    info(uut)
    poke(uut.io.control.req.ready, 1)
  }
}

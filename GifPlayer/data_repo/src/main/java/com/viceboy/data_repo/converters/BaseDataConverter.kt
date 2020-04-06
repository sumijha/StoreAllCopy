package com.viceboy.data_repo.converters

import io.reactivex.Flowable
import io.reactivex.FlowableTransformer

interface BaseDataConverter<IN, OUT> {
    fun convertInToOut(inObject: IN): OUT
    fun convertInToOutFlowableTransformer(inObject: IN): FlowableTransformer<IN, OUT>
    fun convertListInToOut(inObjects: List<IN>): List<OUT>
    fun convertInToOutFlowableTransformer(): FlowableTransformer<IN, OUT>
}

abstract class BaseDataConverterImpl<IN, OUT> : BaseDataConverter<IN, OUT> {
    override fun convertInToOut(inObject: IN): OUT = processConversionFromInToOut(inObject)

    override fun convertListInToOut(inObjects: List<IN>): List<OUT> =
        inObjects.map { processConversionFromInToOut(it) }

    override fun convertInToOutFlowableTransformer(inObject: IN)  = FlowableTransformer<IN,OUT> {
        it.map { convertInToOut(inObject)}
    }

    override fun convertInToOutFlowableTransformer() = FlowableTransformer<IN,OUT> {
        it.flatMap {inObject ->
            processConversionFromInToFlowableOut(inObject)
        }
    }

    protected abstract fun processConversionFromInToFlowableOut(inObject: IN) : Flowable<OUT>

    protected abstract fun processConversionFromInToOut(inObject: IN): OUT
}
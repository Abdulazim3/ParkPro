package com.example.parkingapp.Adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
// extend array adapter to handle autocomplete prediction item
class PlacesAutocompleteAdapter(context: Context, private val placesClient: PlacesClient) :
    ArrayAdapter<AutocompletePrediction>(context, android.R.layout.simple_dropdown_item_1line), Filterable {
    //list holds auto complete predictions
    private var resultList: List<AutocompletePrediction> = arrayListOf()
    private val scope = CoroutineScope(Dispatchers.Main)
    //return the number of predictions
    override fun getCount(): Int = resultList.size
    //returns an item at specific position
    override fun getItem(position: Int): AutocompletePrediction = resultList[position]
    //provides a custome filter object for filtering
    override fun getFilter(): Filter {
        return object : Filter() {
            //defines filtering logic
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                //launch coroutines on the main thread to fetch predictions
                if (constraint != null) {
                    // Asynchronous operation to fetch predictions
                    scope.launch {
                        val predictions = getPredictions(constraint)
                        filterResults.values = predictions
                        filterResults.count = predictions.size
                        publishResults(constraint, filterResults)
                    }
                }
                return filterResults
            }
            //publish results to the adapter
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {

                    resultList = results.values as List<AutocompletePrediction>
                    notifyDataSetChanged()  //notify the adapter to refresh the view
                } else {
                    notifyDataSetInvalidated()  //notify the data set is invalid
                }
            }
        }
    }
    //inflate views for displaying each item
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        val item = getItem(position)
        view.text = item?.getPrimaryText(null).toString()  // Display only the primary text of the suggestion
        return view
    }


    //function to fetch autocomplete predicitons using the places API
    private suspend fun getPredictions(constraint: CharSequence): List<AutocompletePrediction> {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(constraint.toString())
            .build()

        val response = placesClient.findAutocompletePredictions(request).await() //execute and await for response
        return response.autocompletePredictions //returns list of predictions
    }
}


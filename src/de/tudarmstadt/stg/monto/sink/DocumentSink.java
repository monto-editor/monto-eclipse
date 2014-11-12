package de.tudarmstadt.stg.monto.sink;

import java.util.concurrent.Executor;

import org.eclipse.jface.text.Document;
import org.eclipse.swt.widgets.Display;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.tonian.director.dm.json.JSONWriter;

import de.tudarmstadt.stg.monto.message.Languages;
import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.Source;

public class DocumentSink extends Document implements Sink {

	private Source source;
	private Product product;
	private Executor executor;

	public DocumentSink(Source source, Product product) {
		this.source = source;
		this.product = product;
		this.executor = Display.getDefault()::asyncExec;
	}
	
	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	@Override
	public void onProductMessage(ProductMessage message) {
		if(message.getSource().equals(this.source)
		&& message.getProduct().equals(this.product)) {
			
			if(message.getLanguage().equals(Languages.json)) {
				Object obj = JSONValue.parse(message.getContents().getReader());
				JSONWriter writer = new JSONWriter();
				try {
					if(obj instanceof JSONObject) {
						((JSONObject) obj).writeJSONString(writer);
					} else if(obj instanceof JSONArray) {
						((JSONArray) obj).writeJSONString(writer);
					}
					executor.execute(() -> this.set(writer.toString()));
				} catch(Exception e) {
					
				}
			} else { 
				executor.execute(() -> this.set(message.getContents().toString()));			
			}
		}
	}
}

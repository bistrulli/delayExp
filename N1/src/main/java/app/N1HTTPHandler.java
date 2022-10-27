package app;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hubspot.jinjava.Jinjava;
import com.sun.net.httpserver.HttpExchange;

import Server.SimpleTask;
import Server.TierHttpHandler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

@SuppressWarnings("restriction")
public class N1HTTPHandler extends TierHttpHandler {

	public N1HTTPHandler(SimpleTask lqntask, HttpExchange req, long stime) {
		super(lqntask, req, stime);
	}

	public void handleResponse(HttpExchange req, String requestParamValue) throws InterruptedException, IOException {
		this.addToCGV2Group(this.getName());
		this.measureIngress();
		Map<String, String> params = this.getLqntask().queryToMap(req.getRequestURI().getQuery());

		Jinjava jinjava = new Jinjava();
		Map<String, Object> context = Maps.newHashMap();
		context.put("task", this.getLqntask().getName());
		context.put("entry", this.getName());
		
		String renderedTemplate = jinjava.render(this.getWebPageTpl(), context);

//		this.measureEgress();
//		HttpResponse<String> resp = Unirest
//				.get(URI.create("http://localhost:3200/?id=" + params.get("id") + "&entry=e2" + "&snd=e1").toString())
//				.header("Connection", "close").asString();
//		this.measureReturn();

//		Unirest.get(URI.create("http://localhost:3200/?id=" + params.get("id") + "&entry=e1" + "&snd=e1_N1").toString())
//				.header("Connection", "close").asStringAsync();

	
		if (!this.getLqntask().isEmulated()) {
			this.doWorkCPU();
		} else {
			Float executing = 0f;
			String[] entries = this.getLqntask().getEntries().keySet().toArray(new String[0]);
			for (String e : entries) {
				executing += this.getLqntask().getState().get(e + "_ex").get();
			}
			this.doWorkSleep(executing);
		}

		this.measureEgress();

		req.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
		req.getResponseHeaders().set("Cache-Control", "no-store, no-cache, max-age=0, must-revalidate");
		OutputStream outputStream = req.getResponseBody();
		req.sendResponseHeaders(200, renderedTemplate.length());
		outputStream.write(renderedTemplate.getBytes());
		outputStream.flush();
		outputStream.close();
		outputStream = null;
	}

	@Override
	public String getWebPageName() {
		return "tier1.html";
	}

	@Override
	public String getName() {
		return "e1";
	}
}

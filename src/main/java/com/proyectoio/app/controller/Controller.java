package com.proyectoio.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lindo.Lingd17;
import com.proyectoio.app.dominio.Response;

import ch.qos.logback.core.net.SyslogOutputStream;

@RestController
@RequestMapping("/ioProyecto")
public class Controller {
	

	private static final Logger LOG = LoggerFactory.getLogger(Controller.class); 
	private Response TransactionalResponse = new Response(); 
	private Lingd17 lng = new Lingd17();
	
	@RequestMapping( value = "/" , method = RequestMethod.POST , produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
	@ResponseStatus(value = HttpStatus.OK)
	public ObjectNode prueba() {
		return lingo();
	}
	
	 private static int MySolverCallback( Object pnLng, int iLoc, Object jobj)
	    {
	       Controller s = (Controller) jobj;
	       int nIterations[] = new int [1];
	       s.lng.LSgetCallbackInfoIntLng( pnLng, Lingd17.LS_IINFO_ITERATIONS_LNG, nIterations);
	       return( 0);
	    }

	    private static int MyErrorCallback( Object pnLng, int nErrorCode, String jsErrMessage, Object jobj)
	    {
	       return( 0);
	    }
	

		@SuppressWarnings("static-access")
		public ObjectNode  lingo() {
			ObjectNode root=null;
			String body = "";
			Object pLngEnv;
			int nLastIterationCount;
			
		       pLngEnv = lng.LScreateEnvLng();
		       if ( pLngEnv == null)
		       {
		          LOG.info("Unable to create Lingo environment");
		       }

		       double x[] = new double [36];
		       double y[] = new double [36];
		       double v[] = new double [36];

		       int nErr = lng.LSopenLogFileLng( pLngEnv, "lingo.log");
		       if ( nErr != lng.LSERR_NO_ERROR_LNG )
		       {
		    	   LOG.info("LSopenLogFileLng() error: " + nErr);
		       }
		       nErr = lng.LSsetCallbackSolverLng( pLngEnv, "MySolverCallback", this);
		       if ( nErr != lng.LSERR_NO_ERROR_LNG )
		       {
		    	   LOG.info("LSsetCallbackSolverLng() error");
		       }
		       nErr = lng.LSsetCallbackErrorLng( pLngEnv, "MyErrorCallback", this);
		       if ( nErr != lng.LSERR_NO_ERROR_LNG )
		       {
		    	   LOG.info("LSsetCallbackErrorLng() error");
		       }
		       int[] nPointersNow = new int[1];

		       nErr = lng.LSsetPointerLng( pLngEnv, x, nPointersNow);
		       
		       if ( nErr != lng.LSERR_NO_ERROR_LNG )
		       {
		    	   LOG.info("LSsetPointerLng() error");
		       }

		       nErr = lng.LSsetPointerLng( pLngEnv, y, nPointersNow);
		       
		       if ( nErr != lng.LSERR_NO_ERROR_LNG )
		       {
		    	   LOG.info("LSsetPointerLng() error");
		       }
		       
		       nErr = lng.LSsetPointerLng( pLngEnv, v, nPointersNow);
		       
		       if ( nErr != lng.LSERR_NO_ERROR_LNG )
		       {
		    	   LOG.info("LSsetPointerLng() error");
		       }
		       
		       String sScript = "SET ECHOIN 1" + "\n";
		       sScript = sScript + "TAKE Lingo2.lng" + "\n";
		       sScript = sScript + "LOOK ALL" + "\n";
		       sScript = sScript + "GO" + "\n";
		       sScript = sScript + "QUIT" + "\n";

		       nLastIterationCount = -1;
		       nErr = lng.LSexecuteScriptLng( pLngEnv, sScript);
		       if ( nErr != lng.LSERR_NO_ERROR_LNG )
		       {
		          System.out.println( "***LSexecuteScriptLng error***: " + nErr);
		       }

		       nErr = lng.LSclearPointersLng( pLngEnv);
		       if ( nErr != lng.LSERR_NO_ERROR_LNG )
		       {
		          System.out.println( "***LSclearPointerLng() error***: " + nErr);
		       }

		       nErr = lng.LScloseLogFileLng( pLngEnv);
		       if ( nErr != lng.LSERR_NO_ERROR_LNG )
		       {
		          System.out.println( "***LScloseLogFileLng() error***: " + nErr);
		       }
		       
		       
		       String PlanCompra = arrayToJson(x);
		       String PlanInventario = arrayToJson(y);
		       String PlanVenta = arrayToJson(v);

		       ObjectMapper mapper = new ObjectMapper();
		       try {
				JsonNode node1= mapper.readTree(PlanCompra);
				JsonNode node2 = mapper.readTree(PlanInventario);
				JsonNode node3 = mapper.readTree(PlanVenta);
				root = mapper.createObjectNode();
				root.set("Compra", node1);
				root.set("Inventario", node2);
				root.set("Venta", node3);
				
				//body = mapper.writeValueAsString(root);
				
		       } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		       return root;
		}

		public String arrayToJson(double[]a) {
			String CadenaJSON="";
			ObjectMapper mapper = new ObjectMapper();
		       Map<Integer,Double> mapa1 = new HashMap<Integer,Double>();
		       for (int i = 0; i < a.length; i++) {
				mapa1.put(i, a[i]);
		       }
		     try {
				CadenaJSON = mapper.writeValueAsString(mapa1);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return CadenaJSON;
		}
}

package top.greatesky.JreBelService.server;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import net.sf.json.JSONObject;
import top.greatesky.JreBelService.JrebelUtil.JrebelSign;
import top.greatesky.JreBelService.util.rsasign;

public class MainServer extends AbstractHandler {

    public static Map<String, String> parseArguments(String[] args) {
        Map<String, String> params = new HashMap<String, String>();

        String option = null;
        for (final String arg : args) {
            if (arg.charAt(0) == '-') {
                if (arg.length() < 2) {
                    throw new IllegalArgumentException("Error at argument " + arg);
                }
                option = arg.substring(1);
            } else {
                params.put(option, arg);
            }
        }
        return params;
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> arguments = parseArguments(args);
        String port = arguments.get("p");

        if (port == null || !port.matches("\\d+")) {
            port = "8081";
        }

        Server server = new Server(Integer.parseInt(port));
        server.setHandler(new MainServer());
        server.start();

        System.out.println("License Server started at http://localhost:" + port);
        System.out.println("JetBrains Activation address was: http://localhost:" + port + "/");
        System.out.println("JRebel Activation address was: http://localhost:" + port + "/{tokenname}, with any email.");

        server.join();
        
        /***
         * Tip:注册时配置的URL地址：http://localhost:8081/88414687-3b91-4286-89ba-2dc813b107ce1
         * 需要的邮箱：随意输入只要符合邮箱格式就行
         */
    }


    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        System.out.println(target);
        if (target.equals("/")) {
            indexHandler(target, baseRequest, request, response);
        } else if (target.equals("/jrebel/leases")) {
            jrebelLeasesHandler(target, baseRequest, request, response);
        } else if (target.equals("/jrebel/leases/1")) {
            jrebelLeases1Handler(target, baseRequest, request, response);
        } else if (target.equals("/agent/leases")) {
            jrebelLeasesHandler(target, baseRequest, request, response);
        } else if (target.equals("/agent/leases/1")) {
            jrebelLeases1Handler(target, baseRequest, request, response);
        } else if (target.equals("/jrebel/validate-connection")) {
            jrebelValidateHandler(target, baseRequest, request, response);
        } else if (target.equals("/rpc/ping.action")) {
            pingHandler(target, baseRequest, request, response);
        } else if (target.equals("/rpc/obtainTicket.action")) {
            obtainTicketHandler(target, baseRequest, request, response);
        } else if (target.equals("/rpc/releaseTicket.action")) {
            releaseTicketHandler(target, baseRequest, request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private void jrebelValidateHandler(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException  {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        String jsonStr = "{\n" +
                "    \"serverVersion\": \"3.2.4\",\n" +
                "    \"serverProtocolVersion\": \"1.1\",\n" +
                "    \"serverGuid\": \"a1b4aea8-b031-4302-b602-670a990272cb\",\n" +
                "    \"groupType\": \"managed\",\n" +
                "    \"statusCode\": \"SUCCESS\",\n" +
                "    \"company\": \"Administrator\",\n" +
                "    \"canGetLease\": true,\n" +
                "    \"licenseType\": 1,\n" +
                "    \"evaluationLicense\": false,\n" +
                "    \"seatPoolType\": \"standalone\"\n" +
                "}\n";
        JSONObject jsonObject = JSONObject.fromObject(jsonStr);
        String body = jsonObject.toString();
        response.getWriter().print(body);
    }

    private void jrebelLeases1Handler(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        String username = request.getParameter("username");
        baseRequest.setHandled(true);
        String jsonStr = "{\n" +
                "    \"serverVersion\": \"3.2.4\",\n" +
                "    \"serverProtocolVersion\": \"1.1\",\n" +
                "    \"serverGuid\": \"a1b4aea8-b031-4302-b602-670a990272cb\",\n" +
                "    \"groupType\": \"managed\",\n" +
                "    \"statusCode\": \"SUCCESS\",\n" +
                "    \"msg\": null,\n" +
                "    \"statusMessage\": null\n" +
                "}\n";
        JSONObject jsonObject = JSONObject.fromObject(jsonStr);
        if (username != null ) {
            jsonObject.put("company", username);
        }
        String body = jsonObject.toString();
        response.getWriter().print(body);

    }

    private void jrebelLeasesHandler(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        String clientRandomness = request.getParameter("randomness");
        String username = request.getParameter("username");
        String guid = request.getParameter("guid");
        System.out.println(((Request) request).getParameters());
        boolean offline = Boolean.parseBoolean(request.getParameter("offline"));
        String validFrom = "null";
        String validUntil = "null";
        if (offline) {
            String clientTime = request.getParameter("clientTime");
            String offlineDays = request.getParameter("offlineDays");
            //long clinetTimeUntil = Long.parseLong(clientTime) + Long.parseLong(offlineDays)  * 24 * 60 * 60 * 1000;
            long clinetTimeUntil = Long.parseLong(clientTime) + 180L * 24 * 60 * 60 * 1000;
            validFrom = clientTime;
            validUntil = String.valueOf(clinetTimeUntil);
        }
        baseRequest.setHandled(true);
        String jsonStr = "{\n" +
                "    \"serverVersion\": \"3.2.4\",\n" +
                "    \"serverProtocolVersion\": \"1.1\",\n" +
                "    \"serverGuid\": \"a1b4aea8-b031-4302-b602-670a990272cb\",\n" +
                "    \"groupType\": \"managed\",\n" +
                "    \"id\": 1,\n" +
                "    \"licenseType\": 1,\n" +
                "    \"evaluationLicense\": false,\n" +
                "    \"signature\": \"OJE9wGg2xncSb+VgnYT+9HGCFaLOk28tneMFhCbpVMKoC/Iq4LuaDKPirBjG4o394/UjCDGgTBpIrzcXNPdVxVr8PnQzpy7ZSToGO8wv/KIWZT9/ba7bDbA8/RZ4B37YkCeXhjaixpmoyz/CIZMnei4q7oWR7DYUOlOcEWDQhiY=\",\n" +
                "    \"serverRandomness\": \"H2ulzLlh7E0=\",\n" +
                "    \"seatPoolType\": \"standalone\",\n" +
                "    \"statusCode\": \"SUCCESS\",\n" +
                "    \"offline\": " + String.valueOf(offline) + ",\n" +
                "    \"validFrom\": " + validFrom + ",\n" +
                "    \"validUntil\": " + validUntil + ",\n" +
                "    \"company\": \"Administrator\",\n" +
                "    \"orderId\": \"\",\n" +
                "    \"zeroIds\": [\n" +
                "        \n" +
                "    ],\n" +
                "    \"licenseValidFrom\": 1490544001000,\n" +
                "    \"licenseValidUntil\": 1691839999000\n" +
                "}";

        JSONObject jsonObject = JSONObject.fromObject(jsonStr);
        if (clientRandomness == null || username == null || guid == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            JrebelSign jrebelSign = new JrebelSign();
            jrebelSign.toLeaseCreateJson(clientRandomness, guid, offline, validFrom, validUntil);
            String signature = jrebelSign.getSignature();
            jsonObject.put("signature", signature);
            jsonObject.put("company", username);
            String body = jsonObject.toString();
            response.getWriter().print(body);
        }
    }

    private void releaseTicketHandler(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException{
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        String salt = request.getParameter("salt");
        baseRequest.setHandled(true);
        if(salt==null){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }else{
            String xmlContent = "<ReleaseTicketResponse><message></message><responseCode>OK</responseCode><salt>" + salt + "</salt></ReleaseTicketResponse>";
            String xmlSignature = rsasign.Sign(xmlContent);
            String body = "<!-- " + xmlSignature + " -->\n" + xmlContent;
            response.getWriter().print(body);
        }
    }

    private void obtainTicketHandler ( String target , Request baseRequest , HttpServletRequest request ,
                                       HttpServletResponse response ) throws IOException
    {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        SimpleDateFormat fm=new SimpleDateFormat("EEE,d MMM yyyy hh:mm:ss Z", Locale.ENGLISH);
        String date =fm.format(new Date())+" GMT";
        //response.setHeader("Date", date);
        //response.setHeader("Server", "fasthttp");
        String salt = request.getParameter("salt");
        String username = request.getParameter("userName");
        String prolongationPeriod = "607875500";
        baseRequest.setHandled(true);
        if(salt==null||username==null){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }else{
            String xmlContent = "<ObtainTicketResponse><message></message><prolongationPeriod>" + prolongationPeriod + "</prolongationPeriod><responseCode>OK</responseCode><salt>" + salt + "</salt><ticketId>1</ticketId><ticketProperties>licensee=" + username + "\tlicenseType=0\t</ticketProperties></ObtainTicketResponse>";
            String xmlSignature = rsasign.Sign(xmlContent);
            String body = "<!-- " + xmlSignature + " -->\n" + xmlContent;
            response.getWriter().print(body);
        }
    }

    private void pingHandler ( String target , Request baseRequest , HttpServletRequest request , HttpServletResponse response ) throws IOException
    {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        String salt = request.getParameter("salt");
        baseRequest.setHandled(true);
        if(salt==null){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }else{
            String xmlContent = "<PingResponse><message></message><responseCode>OK</responseCode><salt>" + salt + "</salt></PingResponse>";
            String xmlSignature = rsasign.Sign(xmlContent);
            String body = "<!-- " + xmlSignature + " -->\n" + xmlContent;
            response.getWriter().print(body);
        }

    }

    private void indexHandler(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println("<h1>Hello,This is a Jrebel & JetBrains License Server!</h1>");

    }
}
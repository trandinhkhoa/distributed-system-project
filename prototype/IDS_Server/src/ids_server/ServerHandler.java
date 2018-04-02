/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ids_server;

/**
 *
 * @author whoami
 */
public interface ServerHandler {
    public void connect();
    public void requestForWork();
    public void sendResult(String result);
}

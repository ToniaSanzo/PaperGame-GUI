package PaperGame.entities;

import java.io.Serializable;

// Interface for objects transferred between client and server
public interface TransferredObject extends Serializable { public String getType(); /* Return's Object Type*/}

package ${groupId}.exception;

import ${groupId}.api.qeex.annotations.QeexExceptionBundle;
import ${groupId}.qeex.api.annotations.QeexMessage;
import ${groupId}.qeex.api.exceptions.QeexWebException;

@QeexExceptionBundle(project = "FLW", language = "it")
public interface ExceptionBundle {

@QeexMessage(message = "prova")
QeexWebException simpleException();

@QeexMessage(id = 102, code = 400, message = " complete e ricmplete")
QeexWebException completeException();

@QeexMessage(id = 103, code = 500, message = "non sovrascitto")
QeexWebException notOverrideException();

@QeexMessage(id = 104, code = 500, message = "one: %s - two: %s")
QeexWebException withArguments(String one, String two);

}

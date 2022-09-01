%dw 2.0
import countBy from dw::core::Arrays
type ServiceStatus = "HEALTHY" | "UNHEALTHY"
type ServiceType = "HTTP" | "DB" | "JMS" | "AMQP" | "XAPI" | "PAPI"| "SAPI"| "CUSTOM"| "SOAP"
type HealthCheck = {
    name: String,
    "type": String,
    status: ServiceStatus,
    time?: Number | Null,
    error?: String | Null,
    dependencies?: Array<HealthCheck>,
    dependenciesErrors?: Array<HealthCheckError>
}
type HealthCheckError = {
    name: String,
    error: String
}
fun HealthCheckErrorFromInput(name: String, error: String): HealthCheckError = {name:name, error: error}


fun HealthCheckFromDependencies(
    name: String,
    service_type: ServiceType,
    dependencies: Array<HealthCheck>
    ): HealthCheck =
    do {
        var depsInError = dependencies filter $.status == "UNHEALTHY" map {"name" : $.name,"dependenciesErrors": []}
        var finalStatus = if ((depsInError countBy $.status == "UNHEALTHY") > 0) "UNHEALTHY" else "HEALTHY"
        ---
        {
            name: name,
            "type": service_type,
            status: finalStatus,
            time: null,
            error: null,
            dependencies: dependencies,
            dependenciesErrors: depsInError
        }
    }
type FormatProperties = {
    compact: Boolean
}

fun formattedHealthcheck(h:HealthCheck) : HealthCheck = {
    name: h.name,
    "type": h."type",
    status: h.status,
    time: h.time,
    error: h.error,
    dependencies: (h.dependencies default []) map formattedHealthcheck($),
    dependenciesErrors: h.dependenciesErrors map {name: $.name,error: $.error}
}

fun formattedHealthcheck(h:HealthCheck,formatProperties:FormatProperties) : HealthCheck = {
    name: h.name,
    "type": h."type",
    status: h.status,
    (time: h.time) if(!isEmpty(h.time)),
    (error: h.error) if(!isEmpty(h.error)),
    dependencies: (h.dependencies default []) map formattedHealthcheck($,formatProperties),
    (dependenciesErrors: h.dependenciesErrors map {name: $.name,error: $.error} ) if(!isEmpty(h.dependenciesErrors))
}

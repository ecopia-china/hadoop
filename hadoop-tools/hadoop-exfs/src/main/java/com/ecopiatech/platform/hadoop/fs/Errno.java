package com.ecopiatech.platform.hadoop.fs;

public class Errno {
  public static String toString(int errno) {
    switch (errno) {
      case 1:
        return "Operation not permitted"; // EPERM
      case 2:
        return "No such file or directory"; // ENOENT
      case 3:
        return "No such process"; // ESRCH
      case 4:
        return "Interrupted system call"; // EINTR
      case 5:
        return "Input/output error"; // EIO
      case 6:
        return "No such device or address"; // ENXIO
      case 7:
        return "Argument list too long"; // E2BIG
      case 8:
        return "Exec format error"; // ENOEXEC
      case 9:
        return "Bad file descriptor"; // EBADF
      case 10:
        return "No child processes"; // ECHILD
      case 11:
        return "Resource temporarily unavailable"; // EAGAIN
      case 12:
        return "Cannot allocate memory"; // ENOMEM
      case 13:
        return "Permission denied"; // EACCES
      case 14:
        return "Bad address"; // EFAULT
      case 15:
        return "Block device required"; // ENOTBLK
      case 16:
        return "Device or resource busy"; // EBUSY
      case 17:
        return "File exists"; // EEXIST
      case 18:
        return "Invalid cross-device link"; // EXDEV
      case 19:
        return "No such device"; // ENODEV
      case 20:
        return "Not a directory"; // ENOTDIR
      case 21:
        return "Is a directory"; // EISDIR
      case 22:
        return "Invalid argument"; // EINVAL
      case 23:
        return "Too many open files in system"; // ENFILE
      case 24:
        return "Too many open files"; // EMFILE
      case 25:
        return "Inappropriate ioctl for device"; // ENOTTY
      case 26:
        return "Text file busy"; // ETXTBSY
      case 27:
        return "File too large"; // EFBIG
      case 28:
        return "No space left on device"; // ENOSPC
      case 29:
        return "Illegal seek"; // ESPIPE
      case 30:
        return "Read-only file system"; // EROFS
      case 31:
        return "Too many links"; // EMLINK
      case 32:
        return "Broken pipe"; // EPIPE
      case 33:
        return "Numerical argument out of domain"; // EDOM
      case 34:
        return "Numerical result out of range"; // ERANGE
      case 35:
        return "Resource deadlock avoided"; // EDEADLK
      case 36:
        return "File name too long"; // ENAMETOOLONG
      case 37:
        return "No locks available"; // ENOLCK
      case 38:
        return "Function not implemented"; // ENOSYS
      case 39:
        return "Directory not empty"; // ENOTEMPTY
      case 40:
        return "Too many levels of symbolic links"; // ELOOP
      case 42:
        return "No message of desired type"; // ENOMSG
      case 43:
        return "Identifier removed"; // EIDRM
      case 44:
        return "Channel number out of range"; // ECHRNG
      case 45:
        return "Level 2 not synchronized"; // EL2NSYNC
      case 46:
        return "Level 3 halted"; // EL3HLT
      case 47:
        return "Level 3 reset"; // EL3RST
      case 48:
        return "Link number out of range"; // ELNRNG
      case 49:
        return "Protocol driver not attached"; // EUNATCH
      case 50:
        return "No CSI structure available"; // ENOCSI
      case 51:
        return "Level 2 halted"; // EL2HLT
      case 52:
        return "Invalid exchange"; // EBADE
      case 53:
        return "Invalid request descriptor"; // EBADR
      case 54:
        return "Exchange full"; // EXFULL
      case 55:
        return "No anode"; // ENOANO
      case 56:
        return "Invalid request code"; // EBADRQC
      case 57:
        return "Invalid slot"; // EBADSLT
      case 60:
        return "Device not a stream"; // ENOSTR
      case 61:
        return "No data available"; // ENODATA
      case 62:
        return "Timer expired"; // ETIME
      case 63:
        return "Out of streams resources"; // ENOSR
      case 64:
        return "Machine is not on the network"; // ENONET
      case 65:
        return "Package not installed"; // ENOPKG
      case 66:
        return "Object is remote"; // EREMOTE
      case 67:
        return "Link has been severed"; // ENOLINK
      case 68:
        return "Advertise error"; // EADV
      case 69:
        return "Srmount error"; // ESRMNT
      case 70:
        return "Communication error on send"; // ECOMM
      case 71:
        return "Protocol error"; // EPROTO
      case 72:
        return "Multihop attempted"; // EMULTIHOP
      case 73:
        return "RFS specific error"; // EDOTDOT
      case 74:
        return "Bad message"; // EBADMSG
      case 75:
        return "Value too large for defined data type"; // EOVERFLOW
      case 76:
        return "Name not unique on network"; // ENOTUNIQ
      case 77:
        return "File descriptor in bad state"; // EBADFD
      case 78:
        return "Remote address changed"; // EREMCHG
      case 79:
        return "Can not access a needed shared library"; // ELIBACC
      case 80:
        return "Accessing a corrupted shared library"; // ELIBBAD
      case 81:
        return ".lib section in a.out corrupted"; // ELIBSCN
      case 82:
        return "Attempting to link in too many shared libraries"; // ELIBMAX
      case 83:
        return "Cannot exec a shared library directly"; // ELIBEXEC
      case 84:
        return "Invalid or incomplete multibyte or wide character"; // EILSEQ
      case 85:
        return "Interrupted system call should be restarted"; // ERESTART
      case 86:
        return "Streams pipe error"; // ESTRPIPE
      case 87:
        return "Too many users"; // EUSERS
      case 88:
        return "Socket operation on non-socket"; // ENOTSOCK
      case 89:
        return "Destination address required"; // EDESTADDRREQ
      case 90:
        return "Message too long"; // EMSGSIZE
      case 91:
        return "Protocol wrong type for socket"; // EPROTOTYPE
      case 92:
        return "Protocol not available"; // ENOPROTOOPT
      case 93:
        return "Protocol not supported"; // EPROTONOSUPPORT
      case 94:
        return "Socket type not supported"; // ESOCKTNOSUPPORT
      case 95:
        return "Operation not supported"; // EOPNOTSUPP
      case 96:
        return "Protocol family not supported"; // EPFNOSUPPORT
      case 97:
        return "Address family not supported by protocol"; // EAFNOSUPPORT
      case 98:
        return "Address already in use"; // EADDRINUSE
      case 99:
        return "Cannot assign requested address"; // EADDRNOTAVAIL
      case 100:
        return "Network is down"; // ENETDOWN
      case 101:
        return "Network is unreachable"; // ENETUNREACH
      case 102:
        return "Network dropped connection on reset"; // ENETRESET
      case 103:
        return "Software caused connection abort"; // ECONNABORTED
      case 104:
        return "Connection reset by peer"; // ECONNRESET
      case 105:
        return "No buffer space available"; // ENOBUFS
      case 106:
        return "Transport endpoint is already connected"; // EISCONN
      case 107:
        return "Transport endpoint is not connected"; // ENOTCONN
      case 108:
        return "Cannot send after transport endpoint shutdown"; // ESHUTDOWN
      case 109:
        return "Too many references: cannot splice"; // ETOOMANYREFS
      case 110:
        return "Connection timed out"; // ETIMEDOUT
      case 111:
        return "Connection refused"; // ECONNREFUSED
      case 112:
        return "Host is down"; // EHOSTDOWN
      case 113:
        return "No route to host"; // EHOSTUNREACH
      case 114:
        return "Operation already in progress"; // EALREADY
      case 115:
        return "Operation now in progress"; // EINPROGRESS
      case 116:
        return "Stale file handle"; // ESTALE
      case 117:
        return "Structure needs cleaning"; // EUCLEAN
      case 118:
        return "Not a XENIX named type file"; // ENOTNAM
      case 119:
        return "No XENIX semaphores available"; // ENAVAIL
      case 120:
        return "Is a named type file"; // EISNAM
      case 121:
        return "Remote I/O error"; // EREMOTEIO
      case 122:
        return "Disk quota exceeded"; // EDQUOT
      case 123:
        return "No medium found"; // ENOMEDIUM
      case 124:
        return "Wrong medium type"; // EMEDIUMTYPE
      case 125:
        return "Operation canceled"; // ECANCELED
      case 126:
        return "Required key not available"; // ENOKEY
      case 127:
        return "Key has expired"; // EKEYEXPIRED
      case 128:
        return "Key has been revoked"; // EKEYREVOKED
      case 129:
        return "Key was rejected by service"; // EKEYREJECTED
      case 130:
        return "Owner died"; // EOWNERDEAD
      case 131:
        return "State not recoverable"; // ENOTRECOVERABLE
      case 132:
        return "Operation not possible due to RF-kill"; // ERFKILL
      case 133:
        return "Memory page has hardware error"; // EHWPOISON
      default:
        return "Unknow";
    }
  }
}

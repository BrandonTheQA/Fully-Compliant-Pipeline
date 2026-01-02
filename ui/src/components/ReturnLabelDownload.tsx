import React from 'react';
import './ReturnLabelDownload.css';

interface ReturnLabelDownloadProps {
  labelUrl?: string;
  trackingNumber?: string;
  carrier?: string;
}

export const ReturnLabelDownload: React.FC<ReturnLabelDownloadProps> = ({
  labelUrl,
  trackingNumber,
  carrier,
}) => {
  if (!labelUrl) {
    return null;
  }

  return (
    <div className="return-label-download">
      <h3>Return Shipping Label</h3>
      {trackingNumber && (
        <p><strong>Tracking Number:</strong> {trackingNumber}</p>
      )}
      {carrier && (
        <p><strong>Carrier:</strong> {carrier}</p>
      )}
      <a
        href={labelUrl}
        target="_blank"
        rel="noopener noreferrer"
        className="btn btn-primary"
      >
        Download Return Label
      </a>
      <p className="label-instructions">
        Print this label and attach it to your return package. Drop off at any carrier location.
      </p>
    </div>
  );
};


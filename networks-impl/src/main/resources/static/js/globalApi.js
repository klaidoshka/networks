const endpointPrefix = 'http://127.0.0.1:23567/api/v1/graph';

const startLoad = () => {
  $('#loadingModal')
  .modal(
      {
        backdrop: 'static',
        keyboard: false
      }
  )
  .modal('show');
}

const endLoad = () => {
  $('#loadingModal').modal('hide');
}

const onFail = (response) => {
  console.error(response);

  toastr.error(
      `Failed ${response.responseText && `(${response.responseText})`
      || 'to execute. Server error.'}`
  );
}

const onSuccess = (msg) => {
  toastr.success(msg);

  $('#cy-parent').removeClass('bg-light');

  $('#cy').empty();
}

const onGenerateNodesClick = () => {
  $('#generateNodesButton').click(() => {
    const amount = prompt("Enter the amount of nodes to generate:");

    if (amount > 0) {
      startLoad();

      $
      .post(
          `${endpointPrefix}/generate?amount=${amount}`,
          () => onSuccess('Nodes — generated')
      )
      .fail(onFail)
      .always(endLoad);
    } else {
      toastr.error('Invalid amount. Must be greater than 0');
    }
  });
}

const onGenerateLeftSplitNodesClick = () => {
  $('#generateLeftSplitNodesButton').click(() => {
    const amount = prompt("Enter the amount of nodes to generate for the left split:");

    if (amount > 0) {
      startLoad();

      $
      .post(
          `${endpointPrefix}/generateLeftSplit?amount=${amount}`,
          () => onSuccess('Left split nodes — generated')
      )
      .fail(onFail)
      .always(endLoad);
    } else {
      toastr.error('Invalid amount. Must be greater than 0');
    }
  });
}

const onGenerateRightSplitNodesClick = () => {
  $('#generateRightSplitNodesButton').click(() => {
    const amount = prompt("Enter the amount of nodes to generate for the right split:");

    if (amount > 0) {
      startLoad();

      $
      .post(
          `${endpointPrefix}/generateRightSplit?amount=${amount}`,
          () => onSuccess('Right split nodes — generated')
      )
      .fail(onFail)
      .always(endLoad);
    } else {
      toastr.error('Invalid amount. Must be greater than 0');
    }
  });
}

const onDisplayGraphClick = () => {
  $('#displayGraphButton').click(() => {
    startLoad();

    $
    .get(`${endpointPrefix}/display`, (data) => {
      toastr.success('Graph — displayed');

      console.log(data);

      const cy = cytoscape({
        container: document.getElementById('cy'),
        elements: data.cytoscape,
        style: [
          {
            selector: 'node',
            style: {
              'background-color': '#666',
              'label': 'data(id)'
            }
          },
          {
            selector: 'edge',
            style: {
              'width': 3,
              'line-color': '#ccc',
              'target-arrow-color': '#ccc',
              'target-arrow-shape': 'triangle'
            }
          }
        ],
        layout: {
          name: 'cose',
          nodeRepulsion: 10000,
          idealEdgeLength: 100,
          edgeElasticity: 100,
          nestingFactor: 0.1,
        },
        maxZoom: 3,
        minZoom: 0.5,
        styleEnabled: true,
        wheelSensitive: 0.1
      });

      cy.resize();

      cy.fit();

      $('#cy-parent').addClass('bg-light');
    })
    .fail(onFail)
    .always(endLoad);
  });
}

const onDeleteGraphClick = () => {
  $('#deleteButton').click(() => {
    startLoad();

    $
    .post(
        `${endpointPrefix}/delete`,
        () => onSuccess('Graph — deleted')
    )
    .fail(onFail)
    .always(endLoad);
  });
}